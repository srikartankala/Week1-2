import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    int time; // minutes since midnight for easy calculation

    public Transaction(int id, int amount, String merchant, String account, int time) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.time = time;
    }
}

public class FinancialTransactionAnalyzer {

    List<Transaction> transactions;

    public FinancialTransactionAnalyzer(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // -------- Classic Two Sum --------
    public List<String> findTwoSum(int target) {

        HashMap<Integer, Transaction> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                Transaction t2 = map.get(complement);
                result.add("(" + t2.id + "," + t.id + ")");
            }

            map.put(t.amount, t);
        }

        return result;
    }

    // -------- Two Sum within 1 hour --------
    public List<String> findTwoSumWithTime(int target) {

        HashMap<Integer, Transaction> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                Transaction prev = map.get(complement);

                if (Math.abs(t.time - prev.time) <= 60) {
                    result.add("(" + prev.id + "," + t.id + ")");
                }
            }

            map.put(t.amount, t);
        }

        return result;
    }

    // -------- K Sum --------
    public void kSumHelper(int start, int k, int target, List<Integer> path,
                           List<List<Integer>> result) {

        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(path));
            return;
        }

        if (k == 0 || start == transactions.size())
            return;

        for (int i = start; i < transactions.size(); i++) {

            Transaction t = transactions.get(i);

            path.add(t.id);

            kSumHelper(i + 1, k - 1, target - t.amount, path, result);

            path.remove(path.size() - 1);
        }
    }

    public List<List<Integer>> findKSum(int k, int target) {

        List<List<Integer>> result = new ArrayList<>();

        kSumHelper(0, k, target, new ArrayList<>(), result);

        return result;
    }

    // -------- Duplicate Detection --------
    public void detectDuplicates() {

        HashMap<String, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {

            String key = t.amount + "-" + t.merchant;

            map.putIfAbsent(key, new ArrayList<>());

            map.get(key).add(t);
        }

        for (String key : map.keySet()) {

            List<Transaction> list = map.get(key);

            if (list.size() > 1) {

                Set<String> accounts = new HashSet<>();

                for (Transaction t : list)
                    accounts.add(t.account);

                if (accounts.size() > 1) {
                    System.out.println("Duplicate Found → Amount: "
                            + list.get(0).amount
                            + ", Merchant: "
                            + list.get(0).merchant
                            + ", Accounts: "
                            + accounts);
                }
            }
        }
    }

    // -------- MAIN --------
    public static void main(String[] args) {

        System.out.println("Financial Transaction Analyzer");

        List<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction(1, 500, "Store A", "acc1", 600));
        transactions.add(new Transaction(2, 300, "Store B", "acc2", 615));
        transactions.add(new Transaction(3, 200, "Store C", "acc3", 630));
        transactions.add(new Transaction(4, 500, "Store A", "acc2", 650));

        FinancialTransactionAnalyzer analyzer =
                new FinancialTransactionAnalyzer(transactions);

        System.out.println("\nTwo Sum (Target 500):");
        System.out.println(analyzer.findTwoSum(500));

        System.out.println("\nTwo Sum within 1 hour (Target 500):");
        System.out.println(analyzer.findTwoSumWithTime(500));

        System.out.println("\nK Sum (k=3 target=1000):");
        System.out.println(analyzer.findKSum(3, 1000));

        System.out.println("\nDuplicate Transactions:");
        analyzer.detectDuplicates();
    }
}