/*******************************************************************************
 * Copyright © Squid Solutions, 2016
 *
 * This file is part of Open Bouquet software.
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * There is a special FOSS exception to the terms and conditions of the 
 * licenses as they are applied to this program. See LICENSE.txt in
 * the directory of this program distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Squid Solutions also offers commercial licenses with additional warranties,
 * professional functionalities or services. If you purchase a commercial
 * license, then it supersedes and replaces any other agreement between
 * you and Squid Solutions (above licenses and LICENSE.txt included).
 * See http://www.squidsolutions.com/EnterpriseBouquet/
 *******************************************************************************/
package com.squid.kraken.v4.caching.redis.queriesserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squid.kraken.v4.caching.redis.RedisCacheConfig;
import com.squid.kraken.v4.caching.redis.RedisCacheException;
import com.squid.kraken.v4.caching.redis.ServerID;
import com.squid.kraken.v4.caching.redis.queryworkerserver.IQueryWorkerServer;
import com.squid.kraken.v4.caching.redis.queryworkerserver.QueryWorkerFactory;

public class QueriesServer implements IQueriesServer {

    static final Logger logger = LoggerFactory.getLogger(QueriesServer.class);
	
    
    public static enum LoadDistributionPolicy{
    	ROUND_ROBIN, LESS_LOADED 
    }
    
    
    private HashMap<String, ExecutorService> executors;
	private ArrayList<IQueryWorkerServer> workers;
	private boolean debug ;
	
	private int nextWorker=0;
	
	private  LoadDistributionPolicy policy = LoadDistributionPolicy.LESS_LOADED;
	
	private int threadPoolSize=5;

	private HashMap< String, Future<Boolean>> ongoingQueries;
	
	public QueriesServer(){
		executors = new HashMap<String, ExecutorService>();
		ongoingQueries =  new HashMap<String, Future<Boolean>>();
		this.workers = new ArrayList<IQueryWorkerServer>();
		logger.info("new Queries server");
	}

	public QueriesServer(RedisCacheConfig conf){
		this();
		this.debug = conf.getDebug();
		this.threadPoolSize = conf.getQuerieServerThreadPoolSize();
		if (conf.getWorkers() == null){
			// create local worker
				this.workers.add(QueryWorkerFactory.INSTANCE.getNewQueryWorkerServer(conf,new ServerID("localhost", -1), false));
		}else{
			for(ServerID w: conf.getWorkers()){
				this.workers.add(QueryWorkerFactory.INSTANCE.getNewQueryWorkerServer(conf,w , false));
			}
		}	
	}
	
	public String hello(){
		return "Hello Queries Server";
	}

	public void start(){
		logger.info("starting Queries Worker");
		for( IQueryWorkerServer s : this.workers){
			s.start();
		}
	}
	
	
	private  IQueryWorkerServer getNextWorker(){
		if(this.workers.size() == 1){
			return this.workers.get(0);
		}else{
		
			if (this.policy == LoadDistributionPolicy.LESS_LOADED){
				IQueryWorkerServer bestWorkerServ = this.workers.get(0);
				int minimumLoad =bestWorkerServ.getLoad() ;
				if (minimumLoad == 0){
					return bestWorkerServ;
				}
				for (int i= 1 ; i< this.workers.size(); i++){
					int nextLoad = this.workers.get(i).getLoad();
					if (nextLoad == 0){
						return this.workers.get(i) ; //
					}else{
						if (nextLoad < minimumLoad){
							minimumLoad = nextLoad;
							bestWorkerServ = this.workers.get(i);
						}
					}
				}
				return bestWorkerServ;
				
			}else{
				//defaul policy Round Robin
				IQueryWorkerServer nextWorkerServ = this.workers.get(this.nextWorker);
				this.nextWorker =(this.nextWorker+1)% this.workers.size();
				return nextWorkerServ;
			}
		}
	}
	
	@Override
	public boolean fetch(String key, String SQLQuery,  String RSjdbcURL,String username, String pwd, int ttl, long limit){
		if(logger.isDebugEnabled()){logger.debug(("fetch"));}
		Future<Boolean> processingQuery;
		boolean isFirst = false;


		String executorKey ;
		ExecutorService executor;
		if (this.debug)
			executorKey = "debug";
		else
			executorKey = RSjdbcURL;
			
		synchronized(this.executors){
			executor= this.executors.get(executorKey);
			if (executor==null){
				executor = Executors.newFixedThreadPool(threadPoolSize);
				executors.put(executorKey, executor);
			}
		}
		
		synchronized(this.ongoingQueries){
			processingQuery =this.ongoingQueries.get(key) ;	 	
			if (processingQuery==null){
				if(logger.isDebugEnabled()){logger.debug(("new query " + SQLQuery));}
				isFirst=true;
				CallableFetch cf = new CallableFetch(key, SQLQuery, this.getNextWorker(), RSjdbcURL, username, pwd, ttl, limit);
				processingQuery = (Future<Boolean>) executor.submit(cf);
				this.ongoingQueries.put(key, processingQuery);
			}else{
				if(logger.isDebugEnabled()){logger.debug(("ongoing query "+ SQLQuery));}
			}
		}
		boolean failed = true;//check if the processing failed
		try {
			boolean res= processingQuery.get();
			failed = false;// no error
			return res;
		} catch (InterruptedException | ExecutionException e) {
			if (e.getCause()!=null && e.getCause() instanceof RedisCacheException) {
				throw (RedisCacheException)e.getCause();
			} else {
				throw new RedisCacheException(e.getLocalizedMessage());// don't really need the stack trace here
			}
		} finally {
			if (isFirst || failed) {
				synchronized(this.ongoingQueries){
					this.ongoingQueries.remove(key);
				}
			}
		}
	}

	@Override
	public boolean isQueryOngoing(String key) {
		for (IQueryWorkerServer worker : this.workers){
				boolean res = worker.isQueryOngoing(key);
				if (res){
					return true;
				}
		}	
		return false;		
	}
	


}
