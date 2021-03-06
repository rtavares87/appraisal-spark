package appraisal.spark.executor.util

import org.apache.spark.sql._

object Util {
  
  val breastcancer_features = Array[String](
        //"code_number",
        "clump_thickness",
        "uniformity_of_cell_size",
        "uniformity_of_cell_shape",
        "marginal_adhesion",
        "single_epithelial_cell_size",
        "bare_nuclei",
        "bland_chromatin",
        "normal_nucleoli",
        "mitoses")
        //"class")
      
    val aidsocurrence_features = Array[String](
        //"Time",
        //"AIDS_Death",
        "Regulation_on_the_Prevention_and_Treatment_of_AIDS",
        "AIDS_prevention_knowledge",
        "AIDS_awareness",
        "Handwritten_AIDS_newspaper",
        "Handwritten_anti_AIDS_newspaper",
        "AIDS_virus",
        "How_to_prevent_AIDS_A",
        "Route_of_transmission_of_AIDS",
        "AIDS_prevention",
        "What_is_AIDS",
        "Which_day_is_World_AIDS_Day",
        "The_origins_of_the_AIDS_A",
        "The_origins_of_the_AIDS_B",
        "AIDS",
        "AIDS_Day",
        "The_origins_of_AIDS_C",
        "How_to_prevent_AIDS_B",
        "AIDS_awareness_day",
        "World_AIDS_Day",
        "How_to_prevent_AIDS_C",
        "AIDS_awareness_slogan",
        "AIDS_HIV",
        "Initial_symptoms_of_AIDS",
        "Images_of_AIDS_skin_rashes",
        "How_long_will_one_survive_once_he_she_contracts_HIV",
        "How_long_can_AIDS_patients_survive",
        "HIV_AIDS_prevention",
        "AIDS_village_in_Henan_province",
        "How_does_one_contract_HIV",
        "Number_of_AIDS_patients_in_China",
        "Symptoms_of_AIDS_in_incubation_period")
  
  def loadBreastCancer(spark:SparkSession): DataFrame = {
    
    spark.read.option("header", true).csv("C:\\data\\breast-cancer-wisconsin.data.csv")
    
  }
  
  def loadAidsOccurenceAndDeath(spark:SparkSession): DataFrame = {
    
    spark.read.option("header", true).csv("C:\\data\\AIDS Occurrence and Death and Queries.csv")
    
  }
  
  def loadData(spark:SparkSession, filePath:String): DataFrame = {
    
    spark.read.option("header", true).csv(filePath)
    
  }
  
}