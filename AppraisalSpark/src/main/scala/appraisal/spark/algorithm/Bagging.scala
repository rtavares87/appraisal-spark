package appraisal.spark.algorithm

import org.apache.spark.sql._
import appraisal.spark.entities._
import appraisal.spark.util._
import appraisal.spark.strategies._
import scala.collection.mutable.HashMap

class Bagging(plan: ImputationPlan, T: Int) extends Serializable {
  
   var bdf = List.empty[(ImputationPlan)]
   var qT = (1 to T)
   var planName = ""
   
   qT.foreach(_T => {
     
     var nplan = new ImputationPlan(plan.getIdf.sample(true, 1.0), plan.getOdf, plan.getMissingRate, plan.getImputationFeature, plan.getFeatures.clone(), plan.getParallel)
     
     plan.strategies.foreach(strat => {
       
       if (strat.isInstanceOf[SelectionStrategy]) {
          
          val ss = strat.asInstanceOf[SelectionStrategy]
          
          val nparam: HashMap[String, Any] = new HashMap[String, Any]()
          
          val oldparam = ss.params
          oldparam.keySet.foreach(key => nparam.put(key, oldparam.get(key)))
          
          val nalg = ss.selectionAlgorithm.getClass.newInstance()
          
          nplan.addStrategy(new SelectionStrategy(nparam, nalg))
          
       }else if(strat.isInstanceOf[ClusteringStrategy]){
         
         val cs = strat.asInstanceOf[ClusteringStrategy]
         
         val nparam: HashMap[String, Any] = new HashMap[String, Any]()
          
         val oldparam = cs.params
         oldparam.keySet.foreach(key => nparam.put(key, oldparam.get(key)))
          
         val nalg = cs.clusteringAlgorithm.getClass.newInstance()
          
         nplan.addStrategy(new ClusteringStrategy(nparam, nalg))
         
       }else if(strat.isInstanceOf[ImputationStrategy]){
         
         val is = strat.asInstanceOf[ImputationStrategy]
         
         val nparam: HashMap[String, Any] = new HashMap[String, Any]()
          
         val oldparam = is.params
         oldparam.keySet.foreach(key => nparam.put(key, oldparam.get(key)))
          
         val nalg = is.imputationAlgorithm.getClass.newInstance()
          
         nplan.addStrategy(new ImputationStrategy(nparam, nalg))
         
       }
       
     })
     
     nplan.planName = nplan.planName + "->[T=" + _T + "]"
     bdf = bdf :+ nplan
     
   })
   
   def run() :Entities.ImputationResult = {
     
     var impRes = List.empty[(String, Entities.ImputationResult)]
     
     if(plan.getParallel){
     
       bdf.par.foreach(execB => impRes = impRes :+ (execB.planName, execB.run()))
       
     }else{
       
       bdf.foreach(execB => impRes = impRes :+ (execB.planName, execB.run()))
       
     }
     
     if(impRes.size > 1){
       
       val _exec = impRes.filter(_ != null).toArray.sortBy(_._2.avgPercentError).head
       planName = _exec._1
       _exec._2
       
     }else{
       
       val _exec = impRes.head
       planName = _exec._1
       _exec._2
       
     }
     
   }
  
}