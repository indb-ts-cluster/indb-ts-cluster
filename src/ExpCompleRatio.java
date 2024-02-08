import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.SessionDataSet;
import java.util.ArrayList;
import java.util.Random;

/*
    experiment for complementary page ratio
 */
public class ExpCompleRatio {
    public static void main(String[] args) throws IoTDBConnectionException, StatementExecutionException {
        long startTime, endTime;
        String[] dataset = {"air", "ecg", "jinfeng", "tianyuan"};   //datasets
        int[] totalSizes = {714962,700000,1095360,1002240}; //datasizes correspongding to datasets
        double[] ComplementaryRatios = {0,0.2,0.4,0.6,0.8,1};   // ratios
        int cur_comple_index = 5;   // currently testing ratio
        int cur_data_index = 3; // currently testing dataset
        int[] seq_lengths = {166,140,192,96};   // sequence length for corresponding datasets
        String[] ks = {"'3'","'5'","'3'","'3'"};    // k for corresponding datasets
        String l = "'"+seq_lengths[cur_data_index]+"'"; // l for corresponding datasets
        String[] methods = {"kshapeudf", "kshapemudf", "kshape", "kshapem"};
        Session session = new Session("localhost", 6667, "root", "root");
        session.open();
        SessionDataSet result = null;
        String curDataset = dataset[cur_data_index];
        String k = ks[cur_data_index];
        double compleRatio = ComplementaryRatios[cur_comple_index];
        Random random = new Random();
        int iterNum = 10;
        String kshapemr = "'0.2'";
        String[] kshapemeDKS = {"'5'","'7'","'5'","'5'"};
        String kshapemedK = kshapemeDKS[cur_data_index]; // k+2
        for (int i = 0; i < 10; i++) {
            String warmup = "select kshapem(s0,'level'='chunk') from root." + curDataset + ".d0";
            session.executeQueryStatement(warmup);
        }
        LogWriter lw = new LogWriter("./result/ExpCompRatio/" + curDataset + "_res.dat");
        lw.open();
        lw.log("OverlapLen\tKShapeUDF\tKShapeMUDF\tKShape\tKShapeM" + "\n");
        ArrayList<Double> timecosts = new ArrayList<>();
        StringBuilder str = new StringBuilder();
        for (String method : methods) {
            System.out.println(method + ": " + l);
            startTime = System.currentTimeMillis();
            if (method.equals("kshape") || method.equals("kshapem")) {
                iterNum = 5;
            } else if (method.equals("kshapeudf") || method.equals("kshapemudf")) {
                iterNum = 1;
            }
            for (int i = 0; i < iterNum; i++) {
                String sql = "";
                if (method.equals("kshape") || method.equals("kshapem")) {
                    sql = "select " + method + "(s0,'level'='chunk')" +
                            "from root." + curDataset + ".d0";
                } else if (method.equals("kshapeudf")) {
                    sql = "select " + method + "(s0, " + "'k'=" + k + "," +
                            "'l'=" + l + ") " +
                            "from root." + curDataset + ".d0";
                } else if (method.equals("kshapemudf")) {
                    sql = "select " + method + "(s0, " + "'k'=" + k + "," +
                            "'l'=" + l + ",'r'=" + kshapemr + ",'edK'=" +
                            kshapemedK + ")" +
                            " from root." + curDataset + ".d0";
                }
                result = session.executeQueryStatement(sql);
            }
            endTime = System.currentTimeMillis();
            timecosts.add((endTime - startTime) / 1000.0 / iterNum);
        }
        str.append(String.valueOf(compleRatio) + "\t");
        for (double cost : timecosts) {
            str.append(String.format("%.4f", cost) + "\t");
        }
        str.append("\n");
        lw.log(str);
        lw.close();
        session.close();
    }
}