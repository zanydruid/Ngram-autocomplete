package ngram;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;


public class Driver {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        // job1
        Configuration conf1 = new Configuration();
        conf1.set("textinputformat.record.delimiter", ".");
        conf1.set("gram", args[2]);

        Job job1 = Job.getInstance(conf1);
        job1.setJarByClass(Driver.class);

        job1.setMapperClass(NGramLibraryBuilder.NGramMapper.class);
        job1.setReducerClass(NGramLibraryBuilder.NGramReducer.class);

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(IntWritable.class);

        job1.setInputFormatClass(TextInputFormat.class);
        job1.setOutputFormatClass(TextOutputFormat.class);

        TextInputFormat.setInputPaths(job1, new Path(args[0]));
        TextOutputFormat.setOutputPath(job1, new Path(args[1]));

        job1.waitForCompletion(true);

        //job2
        Configuration conf2 = new Configuration();
        conf2.set("threshold", args[3]);
        conf2.set("topN", args[4]);

        DBConfiguration.configureDB(conf2,
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://192.168.1.5:8889/test",
                "root",
                "root");

        Job job2 = Job.getInstance(conf2);
        job2.setJobName("LangModel");
        job2.setJarByClass(Driver.class);

        job2.addArchiveToClassPath(new Path(""));

        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);
        job2.setOutputKeyClass(DBOutputWritable.class);
        job2.setOutputValueClass(NullWritable.class);

        job2.setMapperClass(LanguageModel.LanMap.class);
        job2.setReducerClass(LanguageModel.LanReducer.class);

        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(DBOutputFormat.class);

        DBOutputFormat.setOutput(job2, "output",
                new String[] {"starting_phrase", "following_word", "count"});

        TextInputFormat.setInputPaths(job2, args[1]);
        job2.waitForCompletion(true);
    }
}
