package Java.Util;

import Java.Exeptions.NotEnoughCash;

import java.sql.*;
import java.util.Calendar;

public class DataAccess {
    private static final String url = "jdbc:mysql://localhost:3306?autoReconnect=true&useSSL=false";
    private static final String user = "root";
    private static final String password = "root";

    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;

    public String getPinBySerial(String serial) throws SQLException {
        String query = "SELECT pin FROM bank.cards WHERE id=" + serial;
        return exeQuery(query);
    }
    public Double getBalanceBySerial(String serial) throws SQLException {
        String query = "SELECT balance FROM bank.cards, bank.accounts WHERE cards.id=" +
                serial + " AND accountId = accounts.id";
        return Double.parseDouble(exeQuery(query));
    }
    public void addBalanceBySerial(String serial, double add) throws SQLException {
        double balance = round(getBalanceBySerial(serial) + add);
        String query = "UPDATE bank.accounts, bank.cards SET accounts.balance=" + balance +
                " WHERE accounts.id=cards.accountId AND cards.id=" + serial;
        stmt.execute(query);
    }
    public void oddBalanceBySerial(String serial, double odd) throws SQLException, NotEnoughCash {
        double balance = round(getBalanceBySerial(serial) - odd);
        if(balance < 0.00) throw new NotEnoughCash();
        String query = "UPDATE bank.accounts, bank.cards SET accounts.balance=" + balance +
                " WHERE accounts.id=cards.accountId AND cards.id=" + serial;
        stmt.execute(query);
    }
    public void payBill(String serial, String bill, double amount) throws SQLException, NotEnoughCash {
        Double billAmount = getAmountOfBill(bill);
        addBalanceByAccountId(getAccountIdByBill(bill), amount);
        if(billAmount == amount)
            payFullBull(serial, bill, amount);
        else
            payPartBull(serial, bill, amount, billAmount-amount);
    }
    public Double getAmountOfBill(String bill) throws SQLException {
        String query = "SELECT amount FROM bank.bill WHERE bill.id=" + bill;
        return Double.parseDouble(exeQuery(query));
    }
    public boolean checkDateValid(String serial) throws SQLException {
        String query = "SELECT validUntil FROM bank.cards WHERE cards.id=" + serial;
        rs = stmt.executeQuery(query);
        rs.next();
        Date date = rs.getDate(1);
        return date.after(Calendar.getInstance().getTime()) ? true : false;
    }
    public boolean cardExistence(String serial){
        String query = "SELECT * FROM bank.cards WHERE id=" + serial;
        try {
            stmt.executeQuery(query);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }
    private void addBalanceByAccountId(String id, double add) throws SQLException {
        double balance = round(getBalanceByAccountId(id) + add);
        String query = "UPDATE bank.accounts SET accounts.balance=" + balance +
                " WHERE accounts.id=" + id;
        stmt.execute(query);
    }
    private Double getBalanceByAccountId(String id) throws SQLException {
        String query = "SELECT balance FROM bank.accounts WHERE id=" + id;
        return Double.parseDouble(exeQuery(query));
    }
    private String getAccountIdByBill(String bill) throws SQLException {
        String query = "SELECT accountId FROM bank.bill WHERE id=" + bill;
        return exeQuery(query);
    }
    private void payFullBull(String serial, String bill, double amount) throws SQLException, NotEnoughCash {
        oddBalanceBySerial(serial, amount);
        String query = "DELETE FROM bank.bill WHERE bill.id=" + bill;
        stmt.execute(query);
    }
    private void payPartBull(String serial, String bill, double amount, double newBillAmount) throws SQLException, NotEnoughCash {
        oddBalanceBySerial(serial, amount);
        String query = "UPDATE bank.bill SET amount=" + newBillAmount + " WHERE bill.id=" + bill;
        stmt.execute(query);
    }

    private String exeQuery(String query) throws SQLException {
        rs = stmt.executeQuery(query);
        rs.next();
        return rs.getString(1);
    }
    private static double round(double value) {
        long factor = (long) Math.pow(10, 2);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public void open(){
        try {
            con = DriverManager.getConnection(url, user, password);
            stmt = con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public void close(){
        try {
            con.close();
        } catch (SQLException se) { }
        try {
            stmt.close();
        } catch (SQLException se) {  }
        try {
            rs.close();
        } catch (SQLException se) { }
    }
}
