package com.blogspot.koacervate.update100Kinto10M;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import com.blogspot.koacervate.update100Kinto10M.domain.Customer;
import com.blogspot.koacervate.update100Kinto10M.mapper.CustomerMapper;
import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Update100KInto10MApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(Update100KInto10MApplication.class);
	private static final int BATCH_SIZE = 10000;

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Autowired
	private Gson gson;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Update100KInto10MApplication.class);
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Start to update/insert 100k CSV lines to a database table contains 10M records.");
		CSVReader csvReader = null;
		SqlSession batchSession = null;
		try {
			File csvFile = new File("customers_100k_lines.csv");
			InputStream csvInputStream = new FileInputStream(csvFile);
			csvReader = new CSVReaderBuilder(new InputStreamReader(csvInputStream)).withSkipLines(1).build();

			batchSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			CustomerMapper batchMapper = (CustomerMapper) batchSession.getMapper(CustomerMapper.class);

			log.info(String.format("Total records in CUSTOMER table before: %d", batchMapper.count()));

			long startTime = System.currentTimeMillis();
			int i = 0;
            int skip = 0;
			int inserted = 0;
			String [] x;
			// Read line by from the CSV Input stream
            while ((x = csvReader.readNext()) != null) {
				if (!StringUtils.isEmpty(x[0])) {
					Customer c = null;
					try {
						c = transCustomer(x);
					} catch (Exception e) {
						skip++;
						log.warn(String.format("Can not transfer from String: [ %s ] to a customer object."), String.join(",", x));
					}

					if (null != c) {
						try {
							batchMapper.insertTemp(c);
							i++;
						} catch (Exception e) {
							skip++;
							log.error(String.format("Can not insert customer object: %s"), gson.toJson(c));
						}
						
					}
				} else {
					skip++;
					log.warn(String.format("Can not transfer from String: [ %s ] to a customer object."), String.join(",", x));
				}

				if (i != 0 && i % BATCH_SIZE == 0) {
					try {
						List<BatchResult> batchResults = batchSession.flushStatements();
						inserted += batchResults.size();
						batchSession.commit();
						batchSession.clearCache();
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				}
			}

			// Last batch
			if (i != 0 && i % BATCH_SIZE != 0) {
				try {
					List<BatchResult> batchResults = batchSession.flushStatements();
					inserted += batchResults.size();
					batchSession.commit();
					batchSession.clearCache();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}

			long endTime = System.currentTimeMillis();
			log.info(String.format("Total CSV lines: %d. Total inserted to CUSTOMER_TEMP table: %d by %d batchs. Time spent: %d (ms).", (i + skip), i, inserted, (endTime - startTime)));

			// Merge data from CUSTOMER_TEMP to CUSTOMER table.
			Connection conn = batchSession.getConnection();
			ScriptRunner runner = new ScriptRunner(conn);
			runner.setSendFullScript(true);
			runner.setLogWriter(null);
			Reader r = Resources.getResourceAsReader("sql-scripts/Merge_data_from_CUSTOMER_TEMP_into_CUSTOMER_table.sql");
			long startScript = System.currentTimeMillis();
			runner.runScript(r);
			long endScript = System.currentTimeMillis();
			log.info(String.format("Time spent to merge data from CUSTOMER_TEMP to CUSTOMER table: %d (ms).", (endScript - startScript)));
			log.info(String.format("Total records in CUSTOMER table after: %d", batchMapper.count()));		
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (null != batchSession) {
				batchSession.close();
			}
			if (null != csvReader) {
				try {
					csvReader.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}

		// Truncate table CUSTOMER_TEMP
		SqlSession simpleSession = null;
		try {
			simpleSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, false);
			CustomerMapper simpleMapper = (CustomerMapper) simpleSession.getMapper(CustomerMapper.class);
			simpleMapper.truncateTemp();
			simpleSession.commit();
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (null != simpleSession) {
				simpleSession.close();
			}
		}
		log.info("End to update/insert 100k CSV lines to a database table contains 10M records.");
	}

	private Customer transCustomer(String [] x) {
		// Some concrete business logic here to transfer the CSV lines to Customer objects.
		Customer c = new Customer();
		c.setId(Integer.parseInt(x[0]));
		c.setFirstName(x[1]);
		c.setLastName(x[2]);
		c.setEmail(x[3]);
		c.setCountry(x[4]);
		c.setAppInstall(Boolean.valueOf(x[5]));
		return c;
	}
}
