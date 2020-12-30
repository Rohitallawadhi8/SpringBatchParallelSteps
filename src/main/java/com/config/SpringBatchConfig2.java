/*package com.SpringBatch_CsvToH2AndH2ToCsv.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.SpringBatch_CsvToH2AndH2ToCsv.batch.DBWriter;
import com.SpringBatch_CsvToH2AndH2ToCsv.batch.Processor;
import com.SpringBatch_CsvToH2AndH2ToCsv.batch.Processor2;
import com.SpringBatch_CsvToH2AndH2ToCsv.mapper.UserDBRowMapper.UserDBRowMapper;
import com.SpringBatch_CsvToH2AndH2ToCsv.model.User;


@Configuration
@EnableBatchProcessing
public class SpringBatchConfig2 {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private DBWriter writer1;

	@Autowired
	private Processor processor1;
	
	@Value("${Dept}")
	private String dept;
	
	@Value("${Dept1}")
	private String dept1;
	
	 @Bean
	    public Tasklet tasklet() {
	        return new CountingTasklet();
	    }

	    @Bean
	    public Flow flow1() throws Exception {
	        return new FlowBuilder<Flow>("flow1")
	                .start(stepBuilderFactory.get("step1Flow1")
	                        .tasklet(tasklet()).build())
	                .next(step1())
	                .build();
	    }

	    @Bean
	    public Flow flow2() {
	        return new FlowBuilder<Flow>("flow2")
	                .start(stepBuilderFactory.get("step2")
	                        .tasklet(tasklet()).build())
	                .next(stepBuilderFactory.get("step3")
	                        .tasklet(tasklet()).build())
	                .build();
	    }

	    @Bean()
	    public Job job1() throws Exception {
	        return jobBuilderFactory.get("job")
	                .start(flow1())
	                .split(new SimpleAsyncTaskExecutor()).add(flow2())
	                .end()
	                .build();
	    }

	    public static class CountingTasklet implements Tasklet {

	      

			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				 System.out.println(String.format("%s has been executed on thread %s", chunkContext.getStepContext().getStepName(), Thread.currentThread().getName()));
		            return RepeatStatus.FINISHED;
			}
	    }
	
	

	@Bean
	public Job job() throws Exception {
		return this.jobBuilderFactory.get("BATCH JOB").incrementer(new RunIdIncrementer()).
				start(step1())
				.next(step2())
				.next(step3())
				.build();
	}

	

	@Bean
	public Step step1() throws Exception {
		return stepBuilderFactory.get("step1").<User, User>chunk(100).reader(itemReader()).processor(processor1)
				.writer(writer()).build();
	}

	@Bean
	public Step step2() throws Exception {
		return stepBuilderFactory.get("step2").<User, User>chunk(100).reader(reader2(dept)).processor(processor2())
				.writer(writer2(dept)).build();
	}
	
	@Bean
	
	public Step step3() throws Exception {
		return stepBuilderFactory.get("step3").<User, User>chunk(10).reader(reader2(dept1)).processor(processor2())
				.writer(writer2(dept1)).build();
	}

	@Bean
	public ItemReader<User> itemReader() {

    	System.out.println("inside reader 1");

		FlatFileItemReader<User> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setResource(new FileSystemResource("src/main/resources/users.csv"));
		//flatFileItemReader.setResource(new ClassPathResource("users.csv"));
		flatFileItemReader.setName("CSV-Reader");
		flatFileItemReader.setLinesToSkip(1);
		flatFileItemReader.setLineMapper(lineMapper());
		return flatFileItemReader;
	}

	@Bean
	public LineMapper<User> lineMapper() {

		DefaultLineMapper<User> defaultLineMapper = new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();

		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames(new String[] { "id", "name", "dept", "salary" });

		BeanWrapperFieldSetMapper<User> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(User.class);

		defaultLineMapper.setLineTokenizer(lineTokenizer);
		defaultLineMapper.setFieldSetMapper(fieldSetMapper);

		return defaultLineMapper;
	}

	@Bean
	@Scope(value="prototype")
	public ItemReader<User> reader2(String dept) {
    	System.out.println("inside reader 2");
		JdbcCursorItemReader<User> cursorItemReader = new JdbcCursorItemReader<>();
		cursorItemReader.setDataSource(dataSource);
		cursorItemReader.setSql("select * from user where dept="+"'"+dept+"'");
		cursorItemReader.setRowMapper(new UserDBRowMapper());
		System.out.println(cursorItemReader);
		return cursorItemReader;
	}


	@Bean
	public Processor2 processor2() {
		System.out.println("inside Processor 2!!");
		return new Processor2();
	}

	private Resource outputResource = new FileSystemResource("output/users_output.csv");
	
	@Bean
	public JdbcBatchItemWriter<User> writer() {
		
		JdbcBatchItemWriter<User>  writer=new JdbcBatchItemWriter<>();
		writer.setDataSource(dataSource);
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
		writer.setSql("Insert into user ( id, dept, name, salary, time) values ( :id, :dept, :name, :salary, :time)");
		return writer;
		
	}

	@Bean
	@Scope(value="prototype")
	public FlatFileItemWriter<User> writer2(String dept) {
    	System.out.println("inside writer 2");
    	
    	 Resource outputResource = new FileSystemResource("output/users_output_"+dept+".csv");

		FlatFileItemWriter<User> writer = new FlatFileItemWriter<User>();
		writer.setResource(outputResource);

		DelimitedLineAggregator<User> lineAggregator = new DelimitedLineAggregator<User>();
		lineAggregator.setDelimiter(",");

		BeanWrapperFieldExtractor<User> fieldExtractor = new BeanWrapperFieldExtractor<User>();
		fieldExtractor.setNames(new String[] { "id", "dept", "name", "salary", "time" });
		lineAggregator.setFieldExtractor(fieldExtractor);

		writer.setLineAggregator(lineAggregator);
		writer.setShouldDeleteIfExists(true);
		return writer;
	}



}*/
