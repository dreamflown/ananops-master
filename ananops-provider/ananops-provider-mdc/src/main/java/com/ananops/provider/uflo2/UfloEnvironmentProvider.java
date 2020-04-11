package com.ananops.provider.uflo2;

import com.bstek.uflo.env.EnvironmentProvider;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

@Component
public class UfloEnvironmentProvider implements EnvironmentProvider {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	/**
	 * 实现EnvironmentProvider接口方法
	 *
	 * @return 返回流程引擎需要使用的Hibernate SessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * 实现EnvironmentProvider接口方法
	 *
	 * @return 返回与当前SessionFactory绑定的PlatformTransactionManager对象
	 */
	public PlatformTransactionManager getPlatformTransactionManager() {
		return new JpaTransactionManager(entityManagerFactory);
	}

	/**
	 * 实现EnvironmentProvider接口方法
	 *
	 * getCategoryId方法返回null表示不对流程进行分类处理。只有该值为null 流程设计器里才也可以为空
	 * 该值主要用于saas多租户或者独立部署流程引擎时使用
	 *
	 * @return 返回当前系统分类ID
	 */
	public String getCategoryId() {
		return null;
	}

	/**
	 * 实现EnvironmentProvider接口方法
	 *
	 * getLoginUser方法用于返回当前登录用户的用户id 不是用户名！！！
	 *
	 * @return 返回当前系统的登录用户
	 */
	public String getLoginUser() {
//    	//返回当前系统的登录用户
//		ShiroUser u = ShiroKit.getUser();
		String userId = "anonymous";
//		if (u != null) {
//			userId = u.getId();
//		}
//		System.out.println("getLoginUser:"+userId);
		return userId;
	}

}