import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.SessionDataSet;
import java.util.ArrayList;
import java.util.Random;

/*
    experiment code for different data size
 */
public class ExpSeqNum {
    public static void main(String[] args) throws IoTDBConnectionException, StatementExecutionException {
        long startTime, endTime;
        String[] dataset = {"air", "ecg", "jinfeng", "tianyuan"};
        int[] totalSizes = {714962,700000,10014719,10000128};
//        int[] seqsizes = {400,800,1200,1600,2000,2400,2800,3200,3600,4000}; // ecg air
//        int[] seqsizes = {10000,20000,30000,40000,50000,60000,70000,80000,90000,100000}; // tianyuan
        int[] seqsizes = {5000,10000,15000,20000,25000,30000,35000,40000,45000,50000}; // jinfeng
        int[] seqlens = {166,140,192,96};
        int cur_data_index = 2;
        String[] ks = {"'3'","'5'","'3'","'3'"};
        String k = ks[cur_data_index];
        String l = "'"+seqlens[cur_data_index]+"'";
        String[] methods = {"kshapeudf", "kshapemudf", "kshape", "kshapem"}; //
        Session session = new Session("localhost", 6667, "root", "root");
        session.open();
        SessionDataSet result = null;
        String curDataset = dataset[cur_data_index];
        int totalSize = totalSizes[cur_data_index];
        int seqlen = seqlens[cur_data_index];
        Random random = new Random();
        int iterNum = 10;
        String kshapemr = "'0.2'";
        String[] kshapemeDKS = {"'5'","'7'","'5'","'5'"};
        String kshapemedK = kshapemeDKS[cur_data_index]; // k+2
        for (int i=0; i<3; i++){
            String warmup = "select kshape(s0,'level'='page') from root." + curDataset + ".d0 where time>=1000 and time<10000";
            session.executeQueryStatement(warmup);
        }
        LogWriter lw = new LogWriter("./result/ExpSeqNum/" + curDataset + "_res.dat");
        lw.open();
        lw.log("SeqNum\tKShapeUDF\tKShapeMUDF\tKShape\tKShapeM" + "\n"); // all
        for (int size: seqsizes){
            ArrayList<Double> timecosts = new ArrayList<>();
            StringBuilder str = new StringBuilder();
            for (String method: methods){
                System.out.println(method + ": " + size);
                startTime = System.currentTimeMillis();
                if(method.equals("kshape") || method.equals("kshapem")){
                    iterNum = 1;
                }else if(method.equals("kshapeudf") || method.equals("kshapemudf")){
                    iterNum = 3;
                }
                for(int i=0; i<iterNum; i++){
                    long minTime = random.nextInt(totalSize - (size*seqlen));
                    long maxTime = minTime + (size*seqlen);
                    String sql = "";
                    if (method.equals("kshape") || method.equals("kshapem")){
                        sql = "select " + method + "(s0,'level'='page')" +
                                "from root." + curDataset + ".d0 " +
                                "where time>=" + minTime + " and time<" + maxTime;
                    }else if (method.equals("kshapeudf")){
                        sql = "select " + method + "(s0, " + "'k'=" + k + "," +
                                "'l'=" + l + ") " +
                                "from root." + curDataset + ".d0 " +
                                "where time>=" + minTime + " and time<" + maxTime;
                    }else if (method.equals("kshapemudf")){
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
            str.append(String.valueOf(size) + "\t");
            for(double cost:timecosts){
                str.append(String.format("%.4f", cost) + "\t");
            }
            str.append("\n");
            lw.log(str);
        }
        lw.close();
        session.close();
    }
}
