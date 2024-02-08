import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.SessionDataSet;
import java.util.ArrayList;
import java.util.Random;

/*
    experiment code for different sequence length
 */
public class ExpSeqLen {
    public static void main(String[] args) throws IoTDBConnectionException, StatementExecutionException {
        long startTime, endTime;
        String[] dataset = {"air", "ecg", "jinfeng", "tianyuan"};
        int seqNum = 400;
        int[] totalSizes = {270000,300000,400000,200000}; // datasizes
        int cur_data_index = 3; // change this to change currently testing dataset
        int cur_seq_index = 3;  // change this to change currently testing sequence length
        int[][] seq_lengths = {
                {83,166,332,498,664,830},
                {70,140,280,420,560,700},
                {96,192,384,576,768,960},
                {48,96,192,288,384,480}
        };
        String[] ks = {"'3'","'5'","'3'","'3'"};
        String l = "'"+seq_lengths[cur_data_index][cur_seq_index]+"'";
        String[] methods = {"kshapeudf", "kshapemudf", "kshape", "kshapem"};
        Session session = new Session("localhost", 6667, "root", "root");
        session.open();
        SessionDataSet result = null;
        String curDataset = dataset[cur_data_index];
        String k = ks[cur_data_index];
        int totalSize = totalSizes[cur_data_index];
        int seq_length = seq_lengths[cur_data_index][cur_seq_index];
        int size = seqNum * seq_length;
        Random random = new Random();
        int iterNum = 10;
        String kshapemr = "'0.2'";
        String[] kshapemeDKS = {"'5'","'7'","'5'","'5'"};
        String kshapemedK = kshapemeDKS[cur_data_index]; // k+2
        for (int i = 0; i < 2; i++) {
            String warmup = "select kshapem(s0,'level'='page') from root." + curDataset + ".d0 where time>=1000 and time<5000";
            session.executeQueryStatement(warmup);
        }
        LogWriter lw = new LogWriter("./result/ExpSeqLen/" + curDataset + "_res.dat");
        lw.open();
        lw.log("SeqLen\tKShapeUDF\tKShapeMUDF\tKShape\tKShapeM" + "\n");
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
                            "from root." + curDataset + ".d0 "
                            +
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
        str.append(String.valueOf(seq_length) + "\t");
        for (double cost : timecosts) {
            str.append(String.format("%.4f", cost) + "\t");
        }
        str.append("\n");
        lw.log(str);
        lw.close();
        session.close();
    }
}
