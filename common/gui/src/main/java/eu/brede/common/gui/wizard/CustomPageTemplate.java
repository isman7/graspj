package eu.brede.common.gui.wizard;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.pagetemplates.PageTemplate;

import eu.brede.common.util.GUITools;

/**
 * This class provides a point at which third-party code can
 * introduce custom wrappers around the WizardPages that are displayed.
 * To do so, implement the IPageTemplate interface and wrap this
 * PageTemplate class with your own custom components, delegating the setPage
 * invocation to the wrapped instance of PageTemplate.
 * 
 * @author rcreswick
 *
 */
public class CustomPageTemplate extends PageTemplate {
   
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
   
//   private final CardLayout _layout;// = new CardLayout();
   
   public CustomPageTemplate(){
//	   _layout = new CardLayout() {
//
//		@Override
//		public float getLayoutAlignmentX(java.awt.Container parent) {
//			// TODO Auto-generated method stub
//			return 0f;
//		}
//			
//		};
//      this.setLayout(_layout);
   }
   

   public void setPage(final WizardPage page){
//
//      // remove the page, just in case it was added before:
//      remove(page);
//      validate();
//      
//      add(page, page.getId());
//      _layout.show(this, page.getId());
	   this.removeAll();
	   this.add(page);
	   this.repaint();
	   GUITools.parentPack(this);
   }
}
