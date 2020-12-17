import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Temperature {
  def main(args: Array[String]) {
    val spark = SparkSession.builder.appName("Temperature Second Sort").getOrCreate()
    import spark.implicits._

    val raw_df = spark.read.options(Map("header" -> "true")).csv(args(0))
      .repartition(3)

    val df = raw_df
      .withColumn("_tmp", split($"Time", "\\ "))
      .select(
        $"_tmp".getItem(0).as("Date"),
        col("Temperature")
      ).cache()

    val data = df
      .orderBy("Date")
      .groupBy("Date")
      .agg(sort_array(collect_list("Temperature"), false))

    data.rdd.map(_.toString()).coalesce(1, true).saveAsTextFile(args(1))

    spark.stop()
  }
}
