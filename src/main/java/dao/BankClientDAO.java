package dao;

import exception.DBException;
import model.BankClient;
import util.DBHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BankClientDAO {
    private static BankClientDAO bankClientDAO;

    private BankClientDAO() {
    }

    public static BankClientDAO getInstance() {
        if (bankClientDAO == null) {
            bankClientDAO = new BankClientDAO();
        }
        return bankClientDAO;
    }

    private Connection getConnection() {
        return DBHelper.getMysqlConnection();
    }

    public List<BankClient> getAllBankClient() {
        List<BankClient> list = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, name, password, money FROM bank_client")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(new BankClient(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("password"),
                        resultSet.getLong("money")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean validateClient(String name, String password) {
        boolean result = false;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT password FROM bank_client WHERE name = ?")) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            result = resultSet.getString("password").equals(password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean updateClientsMoney(String senderName, String receiveName, Long transactValue) {
        boolean result = false;
        Connection connection = getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE bank_client SET money = money + ? WHERE name = ?")) {
            connection.setAutoCommit(false);
            statement.setLong(1, -transactValue);
            statement.setString(2, senderName);
            result = statement.executeUpdate() == 1;
            statement.setLong(1, transactValue);
            statement.setString(2, receiveName);
            result = result == true && statement.executeUpdate() == 1;
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return result;
    }

    public BankClient getClientById(long id) throws DBException {
        BankClient bankClient = null;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM bank_client WHERE id = ?")) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                bankClient = new BankClient(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("password"),
                        resultSet.getLong("money"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
        return bankClient;
    }

    public BankClient getClientByName(String name) {
        BankClient bankClient = null;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM bank_client WHERE name = ?")) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                bankClient = new BankClient(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("password"),
                        resultSet.getLong("money"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bankClient;
    }

    public boolean isClientHasSum(String name, Long expectedSum) {
        boolean result = false;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT money FROM bank_client WHERE name = ?")) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getLong("money") >= expectedSum;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public long getClientIdByName(String name) throws DBException {
        long id = 0L;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM bank_client WHERE name = ?")) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getLong("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
        return id;
    }

    public boolean addClient(BankClient client) throws DBException {
        boolean result = false;
        if (getClientByName(client.getName()) == null) {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO bank_client (name, password, money) VALUES (?, ?, ?)")) {
                statement.setString(1, client.getName());
                statement.setString(2, client.getPassword());
                statement.setLong(3, client.getMoney());
                result = statement.executeUpdate() == 1;
            } catch (SQLException e) {
                e.printStackTrace();
                throw new DBException(e);
            }
        }
        return result;
    }

    public boolean deleteClient(String name) {
        boolean result = false;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM bank_client WHERE name = ?")) {
            statement.setString(1, name);
            result = statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void createTable() throws DBException {
        try {
            String sql = "CREATE TABLE  IF NOT  EXISTS bank_client (" +
                    "id bigint auto_increment NOT NULL , " +
                    "name varchar(256) NOT NULL , " +
                    "password varchar(256) NOT NULL , " +
                    "money bigint NOT NULL , " +
                    "primary key (id)" +
                    ")";
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public void dropTable() throws DBException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("DROP TABLE IF EXISTS bank_client")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }
}
