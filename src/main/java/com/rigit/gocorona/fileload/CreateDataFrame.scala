package com.rigit.gocorona.fileload
import org.apache.log4j
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

object CreateDataFrame {

def convertRDDToDF(rdd:RDD[String],sparkSession: SparkSession):DataFrame={

  return sparkSession.createDataFrame(rdd,null);

}


}
