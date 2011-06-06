package org.openspaces.dsl.ui;

public class BarLineChart extends AbstractBasicWidget {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static enum Unit {
        REGULAR,
        PERCENTAGE,
        MEMORY,
        DURATION
    }
    
    private Unit axisYUnit = Unit.REGULAR; 
    public BarLineChart() {
		super();
	}
	
	public BarLineChart(String metric){
		super(metric);
	}

    public Unit getAxisYUnit() {
        return axisYUnit;
    }

    public void setAxisYUnit(Unit axisYUnit) {
        this.axisYUnit = axisYUnit;
    }
}