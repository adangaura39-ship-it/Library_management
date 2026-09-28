package com.library.model;

import java.time.LocalDate;

/**
 * Represents one borrowing transaction: a book issued to a member,
 * with a due date and an optional return date (null while outstanding).
 */
public class Loan extends Entity {

    public static final int LOAN_PERIOD_DAYS = 14;

    private int bookId;
    private int memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate; // null while the book is still out

    // Denormalized display fields, populated by the DAO join for reporting only.
    private String bookTitle;
    private String memberName;

    public Loan(int bookId, int memberId, LocalDate issueDate, LocalDate dueDate) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }

    public Loan(int id, int bookId, int memberId, LocalDate issueDate,
                LocalDate dueDate, LocalDate returnDate) {
        super(id);
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
    }

    public int getBookId() {
        return bookId;
    }

    public int getMemberId() {
        return memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    public boolean isOverdue() {
        return !isReturned() && LocalDate.now().isAfter(dueDate);
    }

    public long daysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    @Override
    public String getSummary() {
        String status = isReturned() ? "returned " + returnDate
                : (isOverdue() ? "OVERDUE by " + daysOverdue() + "d (due " + dueDate + ")"
                : "out, due " + dueDate);
        String book = bookTitle != null ? bookTitle : ("book#" + bookId);
        String member = memberName != null ? memberName : ("member#" + memberId);
        return String.format("[#%d] %s -> %s | %s", id, book, member, status);
    }
}
