import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.SessionDataSet;
import java.util.ArrayList;
import java.util.Random;

/*
    experiment code for cluster number
 */
public class ExpKNum {
    public static void main(String[] args) throws IoTDBConnectionException, StatementExecutionException {
        long startTime, endTime;
        String[] dataset = {"air", "ecg", "jinfeng", "tianyuan"};   // datasets
        int[] totalSizes = {714962,700000,1095360,1002240}; // datasizes correspond to datasets
        int[] kNums = {3,4,5,6,7};  // cluster numbers
        int[] sizes = {700000,690000,1000000,1000000};  // for random shifts
        int cur_k_index = 4;    // currently testing cluster number index
        int cur_data_index = 3; // currently testing dataset
        int[] seq_lengths = {166,140,192,96};   // sequence length corresponding to datasets
        String[] ks = {"'3'","'4'","'5'","'6'","'7'"};
        String l = "'"+seq_lengths[cur_data_index]+"'";
        String[] methods = {"kshapeudf", "kshapemudf", "kshape", "kshapem"};
        Session session = new Session("localhost", 6667, "root", "root");
        session.open();
        SessionDataSet result = null;
        String curDataset = dataset[cur_data_index];
        String k = ks[cur_k_index];
        int totalSize = totalSizes[cur_data_index];
        int size = sizes[cur_data_index];
        Random random = new Random();
        int iterNum = 10;
        int kNum = kNums[cur_k_index];
        String kshapemr = "'0.2'";
        String[] kshapemeDKS = {"'5'","'7'","'5'","'5'"};
        String kshapemedK = kshapemeDKS[cur_data_index]; // k+2
        for (int i = 0; i < 10; i++) {
            String warmup = "select kshapem(s0,'level'='page') from root." + curDataset + ".d0 where time>=1000 and time<5000";
            session.executeQueryStatement(warmup);
        }
        LogWriter lw = new LogWriter("./result/ExpKNum/" + curDataset + "_res.dat");
        lw.open();
        lw.log("kNum\tKShapeUDF\tKShapeMUDF\tKShape\tKShapeM" + "\n");
        ArrayList<Double> timecosts = new ArrayList<>();
        StringBuilder str = new StringBuilder();
        for (String method : methods) {
            System.out.println(method + ": " + l);
            startTime = System.currentTimeMillis();
            if (method.equals("kshape") || method.equals("kshapem")) {
                iterNum = 1;
            } else if (method.equals("kshapeudf") || method.equals("kshapemudf")) {
                iterNum = 1;
            }
            for (int i = 0; i < iterNum; i++) {
                long minTime = random.nextInt(totalSize - size);
                long maxTime = minTime + size;
                String sql = "";
                if (method.equals("kshape") || method.equals("kshapem")) {
                    sql = "select " + method + "(s0,'level'='page')" +
                            "from root." + curDataset + ".d0 " +
                            "where time>=" + minTime + " and time<" + maxTime;
                } else if (method.equals("kshapeudf")) {
                    sql = "select " + method + "(s0, " + "'k'=" + k + "," +
                            "'l'=" + l + ") " +
                            "from root." + curDataset + ".d0 " +
                            "where time>=" + minTime + " and time<" + maxTime;
                } else if (method.equals("kshapemudf")) {
                    sql = "select " + method + "(s0, " + "'k'=" + k + "," +
                            "'l'=" + l + ",'r'=" + kshapemr + ",'edK'=" +
                            kshapemedK + ")" +
                            " from root." + curDataset + ".d0 " +
                            "where time>=" + minTime + " and time<" + maxTime;
                }
                result = session.executeQueryStatement(sql);
            }
            endTime = System.currentTimeMillis();
            timecosts.add((endTime - startTime) / 1000.0 / iterNum);
        }
        str.append(String.valueOf(kNum) + "\t");
        for (double cost : timecosts) {
            str.append(String.format("%.4f", cost) + "\t");
        }
        str.append("\n");
        lw.log(str);
        lw.close();
        session.close();
    }
}