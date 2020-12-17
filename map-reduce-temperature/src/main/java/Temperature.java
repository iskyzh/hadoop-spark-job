import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Temperature {

    public static class DateTemperature implements Writable, WritableComparable<DateTemperature> {
        private String date;
        private Double temperature;

        public DateTemperature() {
        }

        public DateTemperature(String date, Double temperature) {
            this.date = date;
            this.temperature = temperature;
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            this.date = in.readUTF();
            this.temperature = in.readDouble();
        }

        @Override
        public void write(DataOutput out) throws IOException {
            out.writeUTF(this.date);
            out.writeDouble(this.temperature);
        }

        @Override
        public int compareTo(DateTemperature other) {
            int result = this.date.compareTo(other.date);
            if (result != 0) return result;
            return -this.temperature.compareTo(other.temperature);
        }
    }

    public static class DatePartitioner
            extends Partitioner<DateTemperature, DoubleWritable> {
        @Override
        public int getPartition(DateTemperature key, DoubleWritable value,
                                int numPartitions) {
            return ((key.date.hashCode() % numPartitions) + numPartitions) % numPartitions;
        }
    }

    public static class DateGroupingComparator extends WritableComparator {
        public DateGroupingComparator() {
            super(DateTemperature.class, true);
        }

        @Override
        public int compare(WritableComparable wc1, WritableComparable wc2) {
            DateTemperature dt1 = (DateTemperature) wc1;
            DateTemperature dt2 = (DateTemperature) wc2;
            return dt1.date.compareTo(dt2.date);
        }
    }

    public static class CSVMapper
            extends Mapper<Object, Text, DateTemperature, DoubleWritable> {

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            String[] split = value.toString().split(",");
            if (split[1].contains("Temperature")) return;
            Double temperature = Double.parseDouble(split[1]);
            String date = split[0].substring(1, 11);
            DateTemperature dt = new DateTemperature(date, temperature);

            context.write(dt, new DoubleWritable(dt.temperature));
        }
    }

    public static class SecondSortReducer
            extends Reducer<DateTemperature, DoubleWritable, Text, Text> {
        public void reduce(DateTemperature key, Iterable<DoubleWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            StringBuilder temperatures = new StringBuilder();
            for (DoubleWritable val : values) {
                temperatures.append(val);
                temperatures.append(" ");
            }
            context.write(new Text(key.date), new Text(temperatures.toString()));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "temperature cluster");
        job.setJarByClass(Temperature.class);
        job.setMapperClass(CSVMapper.class);
        job.setPartitionerClass(DatePartitioner.class);
        job.setGroupingComparatorClass(DateGroupingComparator.class);
        job.setReducerClass(SecondSortReducer.class);
        job.setOutputKeyClass(DateTemperature.class);
        job.setOutputValueClass(DoubleWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
