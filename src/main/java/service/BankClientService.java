package service;

import dao.BankClientDAO;
import exception.DBException;
import model.BankClient;

import java.util.List;

public class BankClientService {
    private static BankClientService bankClientService;

    private BankClientService() {
    }

    public static BankClientService getInstance() {
        if (bankClientService == null) {
            bankClientService = new BankClientService();
        }
        return bankClientService;
    }

    private static BankClientDAO getBankClientDAO() {
        return BankClientDAO.getInstance();
    }

    public BankClient getClientById(long id) throws DBException {
        return getBankClientDAO().getClientById(id);
    }

    public BankClient getClientByName(String name) {
        return getBankClientDAO().getClientByName(name);
    }

    public List<BankClient> getAllClient() {
        return getBankClientDAO().getAllBankClient();
    }

    public boolean deleteClient(String name) {
        return getBankClientDAO().deleteClient(name);
    }

    public boolean addClient(BankClient client) throws DBException {
        return getBankClientDAO().addClient(client);
    }

    public boolean validateClient(String name, String password) {
        return getBankClientDAO().validateClient(name, password);
    }

    public boolean sendMoneyToClient(BankClient sender, String name, Long value) {
        BankClientDAO bankClientDAO = getBankClientDAO();
        if (bankClientDAO.validateClient(sender.getName(), sender.getPassword()) &&
                bankClientDAO.isClientHasSum(sender.getName(), Math.abs(value)) &&
                getClientByName(name) != null) {
            return getBankClientDAO().updateClientsMoney(sender.getName(), name, value);

        }
        return false;
    }

    public void cleanUp() throws DBException {
        getBankClientDAO().dropTable();
    }

    public void createTable() throws DBException {
        getBankClientDAO().createTable();
    }
}
