package eu.brede.common.config;

import java.util.Date;


public class MetaData extends EnhancedConfig {
	private static final long serialVersionUID = 1L; // try never to change
	
	public MetaData() {
		super();
//		ensureCompliance();
//		put("name","");
//		put("description","");
//		put("dateCreated",new Date());
	}
	
	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("MetaData.");
		requiredConfig.put("name","");
		requiredConfig.put("description","");
		requiredConfig.put("dateCreated",new Date());
		
		return requiredConfig;
	}
	
	public String getName() {
		return getString("name");
	}
	
	public String getName(String defaultName) {
		String name = getString("name");
		if(name.isEmpty()) {
			name = "unnamed";
		}
		return name;
	}

	public void setName(String name) {
		put("name",name);
	}

	public String getDescription() {
		return getString("description");
	}

	public void setDescription(String description) {
		put("description",description);
	}

	public Date getDateCreated() {
		return (Date)get("dateCreated");
	}

	public void setDateCreated(Date dateCreated) {
		put("dateCreated",dateCreated);
	}
}
