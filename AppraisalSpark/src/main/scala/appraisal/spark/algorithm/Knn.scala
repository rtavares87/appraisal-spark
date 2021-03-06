package appraisal.spark.algorithm

import appraisal.spark.entities._
import org.apache.spark.sql._
import org.apache.spark.rdd._
import org.apache.spark.sql.types._
import org.apache.spark.broadcast._
import scala.collection.mutable.ListBuffer
import appraisal.spark.util.Util
import org.apache.spark.sql.functions._
import scala.collection.mutable.HashMap
import appraisal.spark.interfaces.ImputationAlgorithm
import org.apache.spark.broadcast._
import appraisal.spark.statistic.Statistic

class Knn extends ImputationAlgorithm {
  
  def run(idf: DataFrame, params: HashMap[String, Any] = null): Entities.ImputationResult = {
    
    val attribute: String = params("imputationFeature").asInstanceOf[String]
    val calcCol: Array[String] = params("calcFeatures").asInstanceOf[Array[String]]
    
    val k_start: Int = params("k").asInstanceOf[Int]
    var kLimit = params("kLimit").asInstanceOf[Int]
    
    val fidf = idf
    
    val context = fidf.sparkSession.sparkContext
    
    val columns = fidf.columns
    
    val calcDf = fidf.filter(t => t.get(columns.indexOf(attribute)) != null).collect().toList.par
    
    val impDf = fidf.filter(t => t.get(columns.indexOf(attribute)) == null).collect().toList.par
    
    //fidf.unpersist()
    
    if(kLimit > calcDf.size) kLimit = calcDf.size
    
    if(kLimit <= k_start)
      return null
    
    val ks = context.parallelize((k_start to kLimit))
    
    val rdf = impDf.map(row => {
      
      val lineId = row.getLong(columns.indexOf("lineId"))
      val originalValue = row.getDouble(columns.indexOf("originalValue"))
      
      val lIdIndex = columns.indexOf("lineId")
      val oValIndex = columns.indexOf("originalValue")
      val cColPos = calcCol.map(columns.indexOf(_))
      
      val dist = calcDf.map(rowc => (rowc.getLong(lIdIndex), rowc.getDouble(oValIndex) , Util.euclidianDist(row, rowc, cColPos))).toArray
                      .sortBy(_._3)
      
      //val dist = impDf.value.sparkSession.sparkContext.broadcast(Util.euclidianDist(row, calcDf, calcCol).sortBy(_._3).collect())
      //val dist = Util.euclidianDist(row, calcDf, calcCol).sortBy(_._3).collect()
      
      val impByK = ks.map(k => {
      
        val impValue = dist.take(k).map(_._2).reduce((x,y) => x + y) / k
        val error = scala.math.sqrt(scala.math.pow(impValue - originalValue, 2)).doubleValue()
        val percentualError = ((error / originalValue) * 100).doubleValue()
        
        (k, impValue, percentualError)
        
      })
      
      val impByKSelected = impByK.sortBy(_._3, true).first()
      val bestK = impByKSelected._1
      val imputationValue = impByKSelected._2
      
      (lineId, 
       originalValue, 
       imputationValue,
       bestK)})
    
    val count = rdf.size.doubleValue()
    val kavg = rdf.map(_._4).reduce((x,y) => x + y).doubleValue() / count
       
    Statistic.statisticInfo(Entities.ImputationResult(context.parallelize(rdf.map(r => Entities.Result(r._1, r._2, r._3)).toList), 
                                                                              kavg.intValue(), 0, 0, 0, params.toString()))
    
  }
  
  def name(): String = {"Knn"}
  
}