package eu.brede.graspj.gui.configurator;

/**
 * Inspired by ConsoleConfigPanel from andrebossard.com
 */

import ij.ImagePlus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ciscavate.cjwizard.CustomWizardComponent;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.util.GUITools;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.Choice;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.datatypes.Selection;
import eu.brede.graspj.datatypes.WorkflowMap;
import eu.brede.graspj.datatypes.cycle.Cycle;
import eu.brede.graspj.datatypes.cycle.CycleFormatter;
import eu.brede.graspj.datatypes.fitvariable.FitVariable;
import eu.brede.graspj.datatypes.range.RangeComparable;
import eu.brede.graspj.utils.GroupManager.GroupNames;
import eu.brede.graspj.utils.Utils;

/**
 * Stub for displaying Configuration item.
 *
 * @author Norman
 *
 */

// TODO allow for display modification (add a row map?)

public abstract class ItemContainer extends JXTaskPaneContainer implements ConfigFormItem<Object>,CustomWizardComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	final static Logger logger = LoggerFactory.getLogger(ItemContainer.class);
	
	protected DefaultFormBuilder builder;
	
	private boolean childCollapsed = true;
	protected Map<String,ConfigFormItem<?>> components;

//// TODO implement more constructors! Read labels&tooltips from properties?
//	
//	public ItemContainer(ResourceBundle labels, ResourceBundle tooltips) {
//		super();
////		setBorder(BorderFactory.createEmptyBorder());
////		setBorder(BorderFactory.createLineBorder(Color.RED));
//		initBuilder();
//		this.components = new LinkedHashMap<String,ConfigFormItem>();
////		this.labels = labels;
////		this.tooltips = tooltips;
//	}
	
	public ItemContainer() {
		super();
		initBuilder();
		this.components = new LinkedHashMap<String,ConfigFormItem<?>>();
//		this(null, null);
//		this.labels = Global.getBundle("Labels");
//		this.tooltips = Global.getBundle("Descriptions");
	}
	
	protected void initBuilder() {
		builder = new DefaultFormBuilder(initLayout(),
				GUITools.makeParentPacking(initPanel()));
		builder.setDefaultDialogBorder(); //TODO what's that doing?
	}
	
	protected JPanel initPanel() {
		return this;
//		return new JPanel(null);
//		return new ValuePanel();
//		return new FormDebugPanel();
	}
	
//	@SuppressWarnings("serial")
//	private class ValuePanel extends JPanel implements CustomWizardComponent {
//		@Override
//		public Object getValue() {
//			return ItemContainer.this.getValue();
//		}
//	}
	
	protected FormLayout initLayout() {
		 FormLayout layout  = new FormLayout(
//			    "right:max(40dlu;p):GROW(0.0):top, 4dlu, FILL:max(80dlu;DEFAULT):GROW(1.0)", // 1st major column
			    "right:max(20dlu;p):GROW(0.0):top, 4dlu, FILL:max(40dlu;DEFAULT):GROW(1.0)", // 1st major column
//			    "top, 4dlu, FILL:max(80dlu;DEFAULT):GROW(1.0)", // 1st major column
//				"top:pref");
//				"top:pref, top:pref, top:pref");
	 			"center:pref, center:pref, center:pref");
//			    "pref, pref, pref, pref, pref");
		 
		 return layout;
	}
	
	private String getLabelText(String strKey) {
//		if (labels != null && labels.containsKey(strKey)) {
//			return labels.getString(strKey);
//		} else {
//			return strKey;
//		}
		return Global.getStringfromBundle("ConfigLabels", strKey);
	}
	
	private String getTooltipText(String strKey) {
//		if (tooltips != null && tooltips.containsKey(strKey)) {
//			return tooltips.getString(strKey);
//		} else {
//			return strKey;
//		}
		return Global.getStringfromBundle("ConfigDescriptions", strKey);
	}
	
	public JComponent getGUI() {
//		return builder.getPanel();
		return this;
	}

	@Override
	public void setLabelText(String label) {
		// JPanel has no title
	}

	@Override
	public void setToolTipText(String toolTipText) {
		builder.getPanel().setToolTipText(toolTipText);
	}
	
	
//	Map Interface Implementation

//	@Override
	public Object get(Object key) {
		return components.get(key).getValue();
	}


//	@Override
	public Object put(String key, Object value) {
		List<String> flags = new ArrayList<>();
		
		Properties componentProps = new Properties();
		try (InputStream in = getClass().getResourceAsStream( "/" +
				Global.getResourceURI().replace(".", "/") + "Component.properties");) {
			componentProps.load(in);
			String flagString = componentProps.getProperty(key,"");
			flags = Arrays.asList(flagString.split(";"));
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ConfigFormItem component = null;
		BuildType buildType = BuildType.DEFAULT;

//		if(value instanceof EnhancedConfig) {
//			component = new JXTPConfigForm((EnhancedConfig) value);
//		}
		if(value instanceof Option) {
			Option option = (Option) value;
//			if(Configurable.class.isAssignableFrom(this.getClass())) {
//				Configurable cfgable = (Configurable) this;
//				String newPrefix = cfgable.getConfig().getPrefix()
//						+ "." + key + ".";
//				option.setPrefix(newPrefix);
//			}
			option.setPrefix(key + ".");
			component = new OptionItem(option);
			buildType = BuildType.SECTION;
		}
		else if(value instanceof EnhancedConfig) {
			ConfigForm configForm = new ConfigForm(childCollapsed);
			configForm.setConfig((EnhancedConfig) value);
			if(flags.contains("collapse")) {
				configForm.setCollapsed(true);
			}
			if(flags.contains("expand")) {
				configForm.setCollapsed(false);
			}
			component = configForm;
			buildType = BuildType.CONTAINER;
		}
//		else if(value instanceof Configuration) {
//			throw new Error("deprecated");
////			component = new ConfigForm((Configuration) value);
//		}
		else if(value instanceof FitVariable<?>) {
			component = new FitVariableItem((FitVariable<?>)value);
		}
		else if(value instanceof RangeComparable<?>) {
			component = new RangeItem((RangeComparable<? extends Number>)value);
		}
		else if(value instanceof Cycle) {
			CycleFormatter formatter = new CycleFormatter();
			component = new FormattedFieldItem(value, formatter);
		}
		else if(value instanceof String) {
			component = new TextFieldItem((String)value);
		}
		else if(value instanceof Boolean) {
			component = new CheckBoxItem((Boolean)value);
		}
		else if((value instanceof Float) || (value instanceof Double)) {
			DecimalFormat format = new DecimalFormat();
			format.setMaximumFractionDigits(6);
			component = new FormattedFieldItem(value, format);
		}
//		else if(value instanceof Integer) {
//			NumberFormat format = NumberFormat.getIntegerInstance();
//			component = new FormattedFieldItem(value, format);
//		}
		else if(value instanceof Number) {
			component = new FormattedFieldItem(value);
		}
		
//		else if(value instanceof Number) {
////			Format format = NumberFormat.getInstance();
////			component = new FormattedFieldItem(value, format);
//			component = new FormattedFieldItem(value);
//		}
		else if(value instanceof Enum) {
			component = new EnumComboBox((Enum<?>)value);
		}
		else if(value instanceof Choice) {
			component = new EmbeddedChoiceForm((Choice)value);
			buildType = BuildType.SECTION;
		}
		else if(value instanceof File) {
			component = new FileSpecifier((File)value);
		}
		else if(value instanceof ImagePlus) {
			component = new ImagePlusComboBox((ImagePlus)value);
		}
		else if(value instanceof AnalysisItem) {
			component = new AnalysisItemComboBox((AnalysisItem)value);
		}
		else if(value instanceof ObjectChoice) {
			component = new ObjectChoiceItem<>((ObjectChoice<?>)value);
			buildType = BuildType.SECTION;
		}
		else if(value instanceof WorkflowMap) {
//			component = new ObjectListBox<>((WorkflowMap)value);
			component = new WorkflowMapItem((WorkflowMap)value);
			buildType = BuildType.SECTION;
		}
		else if(value instanceof GroupNames) {
			component = new StringSetItem<GroupNames>((GroupNames)value);
		}
		else if(value instanceof CLSystem) {
			component = new CLSystemChoice((CLSystem) value);
			buildType = BuildType.SECTION;
		}
		else if(value instanceof Selection) {
			component = new SelectListBox<>((Selection<?>) value);
		}
		else if(value instanceof JComponent) {
//			component = new ObjectListBox<>((WorkflowMap)value);
			component = new ComponentItem((JComponent)value);
			if(value instanceof JPanel) {
				buildType = BuildType.CONTAINER;
			}
		}
		else {
			component = new UnknownItem(value);
		}
		
		ConfigFormItem oldItem = components.put(key,component);
		
		if(flags.contains("disable")) {
			component.setEnabled(false);
		}
		
		if (!flags.contains("hide")) {
			component.setLabelText(getLabelText(key));
			component.setToolTipText(Utils.htmlify(getTooltipText(key)));
//			super.put(key,component);
//			oldItem = components.put(key,component);
			
//			if((component instanceof JXTPConfigForm) || (component instanceof ItemContainer)){
//				builder.appendRow("pref");
//				builder.setColumn(1);
//		        int columnSpan = builder.getColumnCount();
//		        builder.setColumnSpan(columnSpan);
//				builder.add(component.getGUI());
//				builder.setColumnSpan(1);
//				builder.nextColumn(columnSpan);
//				component.setLabelText(key);
//			}
			
			switch (buildType) {
				case CONTAINER:
					builder.append(component.getGUI(), 3);
					break;
					
				case SECTION:
					JLabel label = new JLabel(getLabelText(key));
					label.setAlignmentY(0f);
					builder.appendRow("top:pref");
					builder.add(label, new CellConstraints(builder.getColumn(), builder.getRow(),
							CellConstraints.DEFAULT, CellConstraints.TOP));
					
					builder.add(component.getGUI(), new CellConstraints(builder.getColumn()+2, builder.getRow(),
							CellConstraints.DEFAULT, CellConstraints.TOP));
					
					builder.nextLine();
					builder.appendRow("top:pref");
					break;
					
				default:
					builder.append(getLabelText(key), component.getGUI());
			}
			
//			if (component instanceof ItemContainer) {
////				builder.appendSeparator(getLabelText(key));
//				builder.append(component.getGUI(), 3);
////				builder.appendSeparator();				
//			}
//			else if((component instanceof OptionItem) || (component instanceof EmbeddedChoiceForm)) {
//				JLabel label = new JLabel(getLabelText(key));
//				label.setAlignmentY(0f);
//				builder.appendRow("top:pref");
//				builder.add(label, new CellConstraints(builder.getColumn(), builder.getRow(),
//						CellConstraints.DEFAULT, CellConstraints.TOP));
//				
//				builder.add(component.getGUI(), new CellConstraints(builder.getColumn()+2, builder.getRow(),
//						CellConstraints.DEFAULT, CellConstraints.TOP));
//				
//				builder.nextLine();
//			}
//			else {
//				builder.append(getLabelText(key), component.getGUI());
//			}
			builder.nextLine();
		}
		else {
//			throw new Error("component is NULL, this cannot happen!");
			logger.info("{} will be hidden",component);
		}
//		GUITools.parentPack(this);
		return oldItem==null?null:oldItem;
	}

//	@Override
//	public int size() {
//		return components.size();
//	}
//
//	@Override
//	public boolean isEmpty() {
//		return components.isEmpty();
//	}
//
//	@Override
//	public boolean containsKey(Object key) {
//		return components.containsKey(key);
//	}
//
//	@Override
//	public boolean containsValue(Object value) {
//		return components.containsValue(value);
//	}
//
//	@Override
//	public Object remove(Object key) {
//		return components.remove(key);
//	}
//
//	@Override
//	public void putAll(Map<? extends String, ? extends Object> m) {
//		for (Map.Entry<? extends String, ? extends Object> e : m.entrySet())
//            put(e.getKey(), e.getValue());
//	}
//
//	@Override
	public void clear() {
		components.clear();
		builder.getPanel().removeAll();
//		initBuilder();
		builder.setColumn(1);
		builder.setRow(1);
//		builder = new DefaultFormBuilder(initLayout(),this);
	}
	
	public void refresh() {
		
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		for(ConfigFormItem item : components.values()) {
			item.setEnabled(enabled);
		}
	}
//
//	@Override
//	public Set<String> keySet() {
//		return components.keySet();
//	}
//
//
//	@Override
//	public Collection<Object> values() {
//		throw new Error("values() not implemented yet");
//	}
//
//	@Override
//	public Set<java.util.Map.Entry<String, Object>> entrySet() {
//		throw new Error("entrySet() not implemented yet");
//	}
	
	public boolean isChildCollapsed() {
		return childCollapsed;
	}

	public void setChildCollapsed(boolean childCollapsed) {
		this.childCollapsed = childCollapsed;
	}

	private enum BuildType {
		DEFAULT,CONTAINER,SECTION;
	}

}