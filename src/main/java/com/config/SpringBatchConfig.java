package com.config;

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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.batch.DBWriter;
import com.batch.Processor;
import com.batch.Processor2;
import com.mapper.UserDBRowMapper.UserDBRowMapper;
import com.model.User;
import com.model.User_Temp;


@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

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
	@Value("${Dept2}")
	private String dept2;
	
	 @Bean
	    public Tasklet tasklet() {
	        return new CountingTasklet();
	    }

	    @Bean
	    public Flow flow1() throws Exception {
	        return new FlowBuilder<Flow>("flow1")
	                .start(stepBuilderFactory.get("step1").
	                		<User, User_Temp>chunk(1).reader(reader(dept))
	                		//.processor(processor1)
	        				.writer(writer()).build())
	          
	                .next(stepBuilderFactory.get("step2")
	                        .tasklet(tasklet()).build())
	                .build();
	    }

	    @Bean
	    public Flow flow2() {
	        return new FlowBuilder<Flow>("flow2")
	                .start(stepBuilderFactory.get("step3").
	                		<User, User_Temp>chunk(1).reader(reader(dept1))
	                		//.processor(processor1)
	        				.writer(writer()).build())
	                
	                .next(stepBuilderFactory.get("step4").
	                		<User, User_Temp>chunk(1).reader(reader(dept2))
	                		//.processor(processor1)
	        				.writer(writer()).build())
	                .next(stepBuilderFactory.get("step5")
	                        .tasklet(tasklet()).build())
	                .build();
	    }

	    @Bean()
	    public Job job() throws Exception {
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
	@Scope(value="prototype")
	public ItemReader<User> reader(String dept) {
    	System.out.println("inside reader 2");
		JdbcCursorItemReader<User> cursorItemReader = new JdbcCursorItemReader<>();
		System.out.println("select * from user where dept="+"'"+dept+"'");
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

	
	@Bean
	@Scope(value="prototype")
	public JdbcBatchItemWriter<User_Temp> writer() {
		
		JdbcBatchItemWriter<User_Temp>  writer=new JdbcBatchItemWriter<>();
		writer.setDataSource(dataSource);
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
		writer.setSql("Insert into user_temp ( id, dept, name, salary, time) values ( :id, :dept, :name, :salary, :time)");
		return writer;
		
	}





}
