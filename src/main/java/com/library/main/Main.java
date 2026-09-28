package com.library.main;

import com.library.exception.BookNotAvailableException;
import com.library.exception.RecordNotFoundException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.service.LibraryService;
import com.library.util.DatabaseManager;
import com.library.util.InputValidator;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final LibraryService service = new LibraryService();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        DatabaseManager.initializeSchema();
        System.out.println("=== Library Management System ===");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = InputValidator.readIntInRange(sc, "Choose an option: ", 0, 12);
            try {
                switch (choice) {
                    case 1 -> addBook();
                    case 2 -> listBooks();
                    case 3 -> searchBooks();
                    case 4 -> updateBook();
                    case 5 -> deleteBook();
                    case 6 -> registerMember();
                    case 7 -> listMembers();
                    case 8 -> issueBook();
                    case 9 -> returnBook();
                    case 10 -> memberHistory();
                    case 11 -> borrowedReport();
                    case 12 -> categoryReport();
                    case 0 -> {
                        running = false;
                        System.out.println("Goodbye!");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (RecordNotFoundException | BookNotAvailableException e) {
                System.out.println(e.getMessage());
            }
        }
        sc.close();
    }

    private static void printMenu() {
        System.out.println("""

                --- Menu ---
                1.  Add book
                2.  List all books
                3.  Search books (title/author)
                4.  Update book
                5.  Delete book
                6.  Register member
                7.  List all members
                8.  Issue book to member
                9.  Return book
                10. Member borrowing history
                11. Report: currently borrowed / overdue books
                12. Report: catalog by category
                0.  Exit
                """);
    }

    // ---------- Books ----------

    private static void addBook() throws SQLException {
        String title = InputValidator.readNonEmpty(sc, "Title: ");
        String author = InputValidator.readNonEmpty(sc, "Author: ");
        String isbn = InputValidator.readNonEmpty(sc, "ISBN: ");
        String category = InputValidator.readNonEmpty(sc, "Category: ");
        int copies = InputValidator.readIntInRange(sc, "Number of copies: ", 1, 10_000);
        Book book = service.addBook(title, author, isbn, category, copies);
        System.out.println("Added: " + book.getSummary());
    }

    private static void listBooks() throws SQLException {
        List<Book> books = service.listBooks();
        if (books.isEmpty()) {
            System.out.println("No books in the catalog yet.");
            return;
        }
        books.forEach(b -> System.out.println(b.getSummary()));
    }

    private static void searchBooks() throws SQLException {
        String keyword = InputValidator.readNonEmpty(sc, "Search title/author: ");
        List<Book> results = service.searchBooks(keyword);
        if (results.isEmpty()) {
            System.out.println("No matches.");
        } else {
            results.forEach(b -> System.out.println(b.getSummary()));
        }
    }

    private static void updateBook() throws SQLException, RecordNotFoundException {
        int id = InputValidator.readInt(sc, "Book ID to update: ");
        Book book = service.getBook(id);
        System.out.println("Current: " + book.getSummary());

        String title = InputValidator.readOptional(sc, "New title (blank to keep): ");
        if (title != null) book.setTitle(title);
        String author = InputValidator.readOptional(sc, "New author (blank to keep): ");
        if (author != null) book.setAuthor(author);
        String category = InputValidator.readOptional(sc, "New category (blank to keep): ");
        if (category != null) book.setCategory(category);

        String copiesStr = InputValidator.readOptional(sc, "New total copies (blank to keep): ");
        if (copiesStr != null) {
            try {
                int newTotal = Integer.parseInt(copiesStr);
                int delta = newTotal - book.getTotalCopies();
                book.setTotalCopies(newTotal);
                book.setAvailableCopies(Math.max(0, book.getAvailableCopies() + delta));
            } catch (NumberFormatException e) {
                System.out.println("Not a number, total copies left unchanged.");
            }
        }
        service.updateBook(book);
        System.out.println("Updated: " + book.getSummary());
    }

    private static void deleteBook() throws SQLException, RecordNotFoundException {
        int id = InputValidator.readInt(sc, "Book ID to delete: ");
        service.deleteBook(id);
        System.out.println("Deleted book #" + id);
    }

    // ---------- Members ----------

    private static void registerMember() throws SQLException {
        String name = InputValidator.readNonEmpty(sc, "Name: ");
        String email = InputValidator.readNonEmpty(sc, "Email: ");
        String phone = InputValidator.readNonEmpty(sc, "Phone: ");
        Member member = service.registerMember(name, email, phone);
        System.out.println("Registered: " + member.getSummary());
    }

    private static void listMembers() throws SQLException {
        List<Member> members = service.listMembers();
        if (members.isEmpty()) {
            System.out.println("No members registered yet.");
            return;
        }
        members.forEach(m -> System.out.println(m.getSummary()));
    }

    // ---------- Borrowing ----------

    private static void issueBook() throws SQLException, RecordNotFoundException, BookNotAvailableException {
        int bookId = InputValidator.readInt(sc, "Book ID: ");
        int memberId = InputValidator.readInt(sc, "Member ID: ");
        Loan loan = service.issueBook(bookId, memberId);
        System.out.println("Issued: " + loan.getSummary());
    }

    private static void returnBook() throws SQLException, RecordNotFoundException {
        int loanId = InputValidator.readInt(sc, "Loan ID: ");
        Loan loan = service.returnBook(loanId);
        System.out.println("Returned: " + loan.getSummary());
    }

    private static void memberHistory() throws SQLException, RecordNotFoundException {
        int memberId = InputValidator.readInt(sc, "Member ID: ");
        Member member = service.getMember(memberId);
        System.out.println("History for " + member.getName() + ":");
        List<Loan> history = service.memberHistory(memberId);
        if (history.isEmpty()) {
            System.out.println("  (no loans yet)");
        } else {
            history.forEach(l -> System.out.println("  " + l.getSummary()));
        }
    }

    // ---------- Reports ----------

    private static void borrowedReport() throws SQLException {
        List<Loan> loans = service.borrowedAndOverdueReport();
        if (loans.isEmpty()) {
            System.out.println("Nothing currently checked out.");
            return;
        }
        System.out.println("Currently borrowed (soonest due first):");
        loans.forEach(l -> System.out.println("  " + l.getSummary()));
    }

    private static void categoryReport() throws SQLException {
        Map<String, Long> counts = service.booksByCategory();
        System.out.println("Titles per category:");
        counts.forEach((category, count) -> System.out.println("  " + category + ": " + count));
    }
}
