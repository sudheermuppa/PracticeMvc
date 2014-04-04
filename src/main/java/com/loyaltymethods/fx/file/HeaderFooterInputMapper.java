
package com.loyaltymethods.fx.file;

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

/**
 * The whole purpose of this class was to add HEADER_FLG and FOOTER_FLG in order to 
 * pass the header/footer through the validation system. However, now the validation system
 * doesn't see this because we filter it out. So it's a bit pointless.
 * 
 * addFields is the map that specifies what to add to header and footer records in order
 * to mark them as header/footer records for any further processing to take notice. Decidedly
 * a dumb idea, and will need to be revised.
 *  
 * @author Emil
 *
 */
public class HeaderFooterInputMapper implements FieldSetMapper<FieldSet> {
	Logger log = Logger.getLogger(HeaderFooterInputMapper.class);
	Map<String, String> addFields;

	public FieldSet mapFieldSet(FieldSet fieldSet) throws BindException {
		ArrayList<String> names;
		ArrayList<String> values;
		
		if( addFields != null ) {
			
			names = new ArrayList<String>();
			values = new ArrayList<String>();
			
			for( String name : addFields.keySet()) {
				names.add(name);
				values.add(addFields.get(name));
			}
			String [] fnames = fieldSet.getNames();
			String [] fvalues = fieldSet.getValues();
			for(int i = 0; i<fnames.length; i++) {
				names.add(fnames[i]);
				values.add(fvalues[i]);
			}
			String [] arrnames = new String[names.size()];
			String [] arrvalues = new String[values.size()];
			
			DefaultFieldSet dfs = new DefaultFieldSet(values.toArray(arrvalues),names.toArray(arrnames));
			
			log.debug("HF Mapper: "+dfs.toString());
			return dfs;
		}
		else {		// if no additional fields, just return pass-through
			log.debug("HF Mapper: "+fieldSet.toString());
			return fieldSet;
		}
	}

	public Map<String, String> getAddFields() {
		return addFields;
	}

	public void setAddFields(Map<String, String> addFields) {
		this.addFields = addFields;
	}

}
