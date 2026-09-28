package com.library.service;

import com.library.dao.BookDAO;
import com.library.dao.LoanDAO;
import com.library.dao.MemberDAO;
import com.library.exception.BookNotAvailableException;
import com.library.exception.RecordNotFoundException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic layer. Keeps an in-memory ISBN -> Book index (HashMap) for
 * fast lookups on top of the List<Book> returned by the DAO, and enforces
 * the domain rules that the console layer should never bypass:
 *   - can't issue a book with zero available copies
 *   - can't issue the same book to the same member twice while unreturned
 */
public class LibraryService {

    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final LoanDAO loanDAO = new LoanDAO();

    // ---------- Books ----------

    public Book addBook(String title, String author, String isbn, String category, int copies) throws SQLException {
        return bookDAO.create(new Book(title, author, isbn, category, copies));
    }

    public List<Book> listBooks() throws SQLException {
        return bookDAO.findAll();
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        return bookDAO.search(keyword);
    }

    public Book getBook(int id) throws SQLException, RecordNotFoundException {
        return bookDAO.findById(id);
    }

    public void updateBook(Book book) throws SQLException, RecordNotFoundException {
        bookDAO.update(book);
    }

    public void deleteBook(int id) throws SQLException, RecordNotFoundException {
        bookDAO.delete(id);
    }

    /** ISBN -> Book lookup map, built fresh from the current catalog (HashMap usage). */
    public Map<String, Book> buildIsbnIndex() throws SQLException {
        Map<String, Book> index = new HashMap<>();
        for (Book b : bookDAO.findAll()) {
            index.put(b.getIsbn(), b);
        }
        return index;
    }

    // ---------- Members ----------

    public Member registerMember(String name, String email, String phone) throws SQLException {
        return memberDAO.create(new Member(name, email, phone));
    }

    public List<Member> listMembers() throws SQLException {
        return memberDAO.findAll();
    }

    public List<Member> searchMembers(String keyword) throws SQLException {
        return memberDAO.search(keyword);
    }

    public Member getMember(int id) throws SQLException, RecordNotFoundException {
        return memberDAO.findById(id);
    }

    public void updateMember(Member member) throws SQLException, RecordNotFoundException {
        memberDAO.update(member);
    }

    public void deleteMember(int id) throws SQLException, RecordNotFoundException {
        memberDAO.delete(id);
    }

    // ---------- Borrowing ----------

    /**
     * Issues a book to a member. Refuses if there are no available copies
     * (BookNotAvailableException) or if this exact member already has this
     * exact book out and unreturned.
     */
    public Loan issueBook(int bookId, int memberId) throws SQLException, RecordNotFoundException, BookNotAvailableException {
        Book book = bookDAO.findById(bookId);
        memberDAO.findById(memberId); // validates the member exists

        if (!book.isAvailable()) {
            throw new BookNotAvailableException(
                    "\"" + book.getTitle() + "\" has no available copies right now.");
        }
        if (loanDAO.hasActiveLoan(bookId, memberId)) {
            throw new BookNotAvailableException(
                    "This member already has \"" + book.getTitle() + "\" checked out.");
        }

        LocalDate today = LocalDate.now();
        Loan loan = new Loan(bookId, memberId, today, today.plusDays(Loan.LOAN_PERIOD_DAYS));
        loanDAO.create(loan);
        bookDAO.adjustAvailableCopies(bookId, -1);
        return loan;
    }

    public Loan returnBook(int loanId) throws SQLException, RecordNotFoundException {
        Loan loan = loanDAO.findById(loanId);
        if (loan.isReturned()) {
            throw new RecordNotFoundException("Loan #" + loanId + " was already returned on " + loan.getReturnDate());
        }
        loanDAO.markReturned(loanId, LocalDate.now());
        bookDAO.adjustAvailableCopies(loan.getBookId(), +1);
        return loanDAO.findById(loanId);
    }

    public List<Loan> memberHistory(int memberId) throws SQLException {
        return loanDAO.findByMember(memberId);
    }

    // ---------- Reports ----------

    /** Currently borrowed and/or overdue books, sorted by due date (soonest/most overdue first). */
    public List<Loan> borrowedAndOverdueReport() throws SQLException {
        List<Loan> active = loanDAO.findActiveLoans();
        return active.stream()
                .sorted(Comparator.comparing(Loan::getDueDate))
                .collect(Collectors.toList());
    }

    public List<Loan> overdueOnly() throws SQLException {
        return loanDAO.findOverdueLoans();
    }

    /** Category -> count of distinct titles, for a quick catalog breakdown (TreeMap keeps it alphabetized). */
    public Map<String, Long> booksByCategory() throws SQLException {
        return bookDAO.findAll().stream()
                .collect(Collectors.groupingBy(
                        Book::getCategory,
                        java.util.TreeMap::new,
                        Collectors.counting()));
    }
}
