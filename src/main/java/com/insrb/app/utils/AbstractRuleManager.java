package com.insrb.app.utils;

import java.util.HashMap;

public abstract class AbstractRuleManager<T> {

	public T getCurrentRule(String key) {
		T current=rep.getRule(key);
		if(current==null) {
			return defaultInstance();
		}
		return current;
	}
	static protected class RuleRepository<T>{
		private HashMap<String,T> map=new  HashMap<String,T>(0);
		public void registRule(String key, T ruleInstance) {
			map.put(key, ruleInstance);
		}
		public T getRule(String key) {
			return map.get(key);
		}
	}
	private RuleRepository<T> rep=new RuleRepository<T>();
	public AbstractRuleManager(){
		onRegistrationRules(rep);
	}
	protected abstract void onRegistrationRules(RuleRepository<T> rep);
	abstract protected T defaultInstance();
}
