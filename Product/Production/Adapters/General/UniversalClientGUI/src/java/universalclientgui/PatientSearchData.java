/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package universalclientgui;

import com.sun.webui.jsf.component.Checkbox;
import java.util.ArrayList;
import java.util.List;


public class PatientSearchData {

    private ArrayList<String> columns;

    public PatientSearchData(List<String> cols) {
        System.out.println("Construct new PatientSearchData");
        columns = new ArrayList<String>(cols);
        //tspl = new TableSelectPhaseListener();
    }

    public ArrayList<String> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<String> columns) {
        this.columns = columns;
    }

    public String getLastName() {
        return columns.get(0);
    }

    public void setLastName(String col) {
        columns.set(0, col);
    }

    public String getFirstName() {
        return columns.get(1);
    }

    public void setFirstName(String col) {
        columns.set(1, col);
    }

    public String getPatientId() {
        return columns.get(2);
    }

    public void setPatientId(String col) {
        columns.set(2, col);
    }

    public String getSsn() {
        return columns.get(3);
    }

    public void setSsn(String col) {
        columns.set(3, col);
    }

    public String getDob() {
        return columns.get(4);
    }

    public void setDob(String col) {
        columns.set(4, col);
    }

    public String getGender() {
        return columns.get(5);
    }

    public void setGender(String col) {
        columns.set(5, col);
    }
 
}
