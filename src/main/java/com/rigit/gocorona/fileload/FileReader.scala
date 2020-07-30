package com.rigit.gocorona.fileload

import com.rigit.gocorona.caseclass.{IndiaDataSet, IndianPopulation}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, SparkSession}


object FileReader {

    def readFile(filename:String,sc:SparkContext): RDD[String] ={
        return sc.textFile(filename);
    }

    def readFileToDF(filename:String,sparkSession: SparkSession): DataFrame ={
        return sparkSession.read.format("csv").option("header","true")
            .option("inferSchema","true")
            .option("nullValue","-")
        .option("path",filename)
        .load();
    }

    def readFileToDS(filename: String, sparkSession: SparkSession): Dataset[IndiaDataSet] ={
        import sparkSession.sqlContext.implicits._;
        return sparkSession.read.option("header","true")
          .option("inferSchema","true")
            .option("nullValue","-")
          .csv(filename).as[IndiaDataSet]


    }

    def readPopulationFileToDS(filename: String, sparkSession: SparkSession) : Dataset[IndianPopulation] = {
        import sparkSession.sqlContext.implicits._;
        return sparkSession.read.option("header","true")
            .option("inferSchema","true")
            .option("header","true")
            .csv(filename)
            .as[IndianPopulation]
    }

}
