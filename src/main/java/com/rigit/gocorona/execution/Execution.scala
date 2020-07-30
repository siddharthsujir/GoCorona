package com.rigit.gocorona.execution

import java.util.Date

import com.rigit.gocorona.caseclass.{IndiaDataSet, IndianPopulation}
import com.rigit.gocorona.fileload.{CreateDataFrame, FileNames, FileReader}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Column, DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions.{round,udf,when,expr}

object Execution {

  def fileprocessing(sc: SparkContext, sparkSession: SparkSession): Unit = {
    // Importing COVID India DS
    val covid_India_DS = FileReader.readFileToDS(FileNames.COVID_INDIA, sparkSession)
    covid_India_DS.persist();
    covid_India_DS.explain()
    covid_India_DS.show(100)


    // Importing Population India DS

    val population_India_DS = FileReader.readPopulationFileToDS(FileNames.POPULATION_INDIA, sparkSession);
    population_India_DS.persist();
    val caseCountByStates = getCaseCountByStateDS(covid_India_DS, population_India_DS);
    caseCountByStates.explain()
    caseCountByStates.coalesce(1).write.mode("overwrite").format("csv").save("C:\\Users\\siddhu\\Documents\\Projects\\SparkStreamingApp\\output")

    //    var totalCasesDF=FileReader.readFileToDF("C:\\Users\\siddhu\\Downloads\\covid_19_india.csv",sparkSession)
    //    totalCasesDF.persist();
    //    totalCasesDF.show(100);
    //
    //    var string = totalCasesDF.foreach(s=>println(s))
    //
    //
    //    var df=getCaseCountByState(totalCasesDF);
    //    df.show(50);
  }

  def getHeadersFromRDD(rdd: RDD[String]): RDD[String] = {
    val header = rdd.first();
    return rdd.filter(row => row != header)
  }

  def getCaseCountByState(case_df: DataFrame): DataFrame = {
    var IndiaDF = case_df.select("State/UnionTerritory", "Confirmed", "Cured", "Deaths")
      .groupBy("State/UnionTerritory")
      .sum("confirmed", "Cured", "Deaths")
      .orderBy(org.apache.spark.sql.functions.col("sum(confirmed)").desc)

    return IndiaDF


  }

  def getCaseCountByStateDS(case_df: Dataset[IndiaDataSet], population_ds: Dataset[IndianPopulation]): Dataset[Row] = {
    var IndiaDF = case_df.select("State_Union_Territory", "Confirmed", "Cured", "Deaths")
      .groupBy("State_Union_Territory")
      .sum("confirmed", "Cured", "Deaths")
      .orderBy(org.apache.spark.sql.functions.col("sum(confirmed)").desc)

    var joinWithPopulation = IndiaDF.join(population_ds, IndiaDF("State_Union_Territory")===population_ds("StateUnionTerritory"))
      .withColumn("Confirmed",IndiaDF("sum(confirmed)"))
      .withColumn("Death",IndiaDF("sum(deaths)"))
      .withColumn("Cured",IndiaDF("sum(cured)"))
      .withColumn("DeathPerPopulationRatio", round(getDeathPerPopulation(IndiaDF("sum(Deaths)"),population_ds("Population"))))
      .withColumn("CaseStatus",when(IndiaDF("sum(Deaths)") > 1000,"Severe").otherwise("Condition Ok"))
      .withColumn("ActiveStatus",expr("Confirmed - Death - Cured"))
//      .withColumn("Covid_Status",expr("case when ActiveStatus > 1000 then 'Still Worrisome' "
//        + "when ActiveStatus >500 && ActiveStatus < 1000 then 'Better' "
//        + "else 'Situation is Normal' end"))
      .select("StateUnionTerritory", "Confirmed", "Death", "Cured", "Population","DeathPerPopulationRatio","CaseStatus","ActiveStatus")
    return joinWithPopulation


  }

  def getDeathPerPopulation(col: Column,col1:Column): Column = {
    return col/col1;
  }

}
