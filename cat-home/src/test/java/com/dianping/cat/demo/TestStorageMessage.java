package com.dianping.cat.demo;

import java.util.Random;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TestStorageMessage {

	private String JDBC_CONNECTION = "jdbc:mysql://%s:3306/%s?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true";

	@Test
	public void testCross() throws Exception {
		String serverIp = "10.10.10.1";

		for (int j = 0; j < 1000000; j++) {
			for (int i = 0; i < 2; i++) {
				sendCacheMsg("cache-1", "user-" + i, "get", serverIp + i);
				sendCacheMsg("cache-1", "user-" + i, "remove", serverIp + i);
				sendCacheMsg("cache-1", "user-" + i, "add", serverIp + i);
				sendCacheMsg("cache-1", "user-" + i, "mGet", serverIp + i);

				sendSQLMsg("sql-1", "user-" + i, "select", serverIp + i);
				sendSQLMsg("sql-1", "user-" + i, "insert", serverIp + i);
				sendSQLMsg("sql-1", "user-" + i, "delete", serverIp + i);
				sendSQLMsg("sql-1", "user-" + i, "update", serverIp + i);

				sendCacheMsg("cache-2", "user-" + i, "get", serverIp + i);
				sendCacheMsg("cache-2", "user-" + i, "add", serverIp + i);
				sendCacheMsg("cache-2", "user-" + i, "remove", serverIp + i);
				sendCacheMsg("cache-2", "user-" + i, "mGet", serverIp + i);
//
				sendSQLMsg("sql-2", "user-" + i, "select", serverIp + i);
				sendSQLMsg("sql-2", "user-" + i, "update", serverIp + i);
				sendSQLMsg("sql-2", "user-" + i, "delete", serverIp + i);
				sendSQLMsg("sql-2", "user-" + i, "insert", serverIp + i);
			}
//			Thread.sleep(5);
		}
	}

	private void sendCacheMsg(String name, String domain, String method, String serverIp) throws InterruptedException {
		Transaction t = Cat.newTransaction("Cache.redis-" + name, "oUserAuthLevel:" + method);

		Cat.logEvent("Cache.redis.server", serverIp);

		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
		((DefaultMessageTree) tree).setDomain(domain);
		Thread.sleep(5 + new Random().nextInt(10));
		int nextInt = new Random().nextInt(3);
		if (nextInt % 2 == 0) {
			t.setStatus(Transaction.SUCCESS);
		} else {
			t.setStatus(String.valueOf(nextInt));
		}
		t.complete();
	}

	private void sendSQLMsg(String name, String domain, String method, String serverIp) throws InterruptedException {
		Transaction t = Cat.newTransaction("SQL", "sql.method");

		Cat.logEvent("SQL.Method", method);
		Cat.logEvent("SQL.Database", String.format(JDBC_CONNECTION, serverIp, name));

		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

		((DefaultMessageTree) tree).setDomain(domain);
		Thread.sleep(3 + new Random().nextInt(2));
		int nextInt = new Random().nextInt(5);

		if (nextInt % 5 == 0) {
			t.setStatus(Transaction.SUCCESS);
		} else {
			t.setStatus(String.valueOf(nextInt));
		}

		t.complete();
	}
}
