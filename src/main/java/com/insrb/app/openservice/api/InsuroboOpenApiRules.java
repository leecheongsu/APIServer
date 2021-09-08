package com.insrb.app.openservice.api;

import com.insrb.app.utils.AbstractRuleManager;

public class InsuroboOpenApiRules {
	public interface InsuroboOpenApiRule {

		boolean useExceptionCaseRule(String currentCase);

	}
	private InsuroboOpenApiRules() {}
	private static InsuroboOpenApiRule default_instance=new InsuroboOpenApiRule() {

		@Override
		public boolean useExceptionCaseRule(String currentCase) { return false; }
		
	};
	static private AbstractRuleManager<InsuroboOpenApiRule> manager=new AbstractRuleManager<InsuroboOpenApiRule>() {

		@Override
		public void onRegistrationRules(RuleRepository<InsuroboOpenApiRule> rep) {
			rep.registRule("OpenServiceForInsrbBuildingAssessment.quoteAssessmentForSingle",
					new InsuroboOpenApiRule() {
						@Override
						public boolean useExceptionCaseRule(String currentCase) {
							if("building_type:null".equals(currentCase)) {
								return true;
							}
							return true;
						}
			});
			rep.registRule("OpenServiceForInsrbBuildingAssessment.quoteAssessmentForGroup",
					new InsuroboOpenApiRule() {
						@Override
						public boolean useExceptionCaseRule(String currentCase) {
							if("building_type:null".equals(currentCase)) {
								return true;
							}
							if("mainPurpsCdNm:근린".equals(currentCase)) {
								return false;
							}
							return true;
						}
			});
		}

		@Override
		protected InsuroboOpenApiRule defaultInstance() {
			return default_instance;
		}
		
	};
	public static InsuroboOpenApiRule getCurrentRule(String key) {
		return manager.getCurrentRule(key);
	}
}
