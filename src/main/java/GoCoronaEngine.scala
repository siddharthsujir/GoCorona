import com.rigit.gocorona.execution.Execution
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object GoCoronaEngine extends App {

  override def main(args: Array[String]): Unit = {

    Console.print("Staring the Spark Application!")
    var conf=new SparkConf()
      .setMaster("local[*]")
      .setAppName("GoCorona");
    var sc=SparkContext.getOrCreate(conf);
    var sparkSession=SparkSession.builder()
      .master("local[*]")
      .config(conf)
      .getOrCreate();
    Console.print(sparkSession.conf)
    Execution.fileprocessing(sc,sparkSession);

    Thread.sleep(100000000)
  }

}
