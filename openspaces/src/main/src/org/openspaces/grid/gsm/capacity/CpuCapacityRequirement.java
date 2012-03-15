/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.grid.gsm.capacity;

import org.openspaces.core.internal.commons.math.ConvergenceException;
import org.openspaces.core.internal.commons.math.fraction.Fraction;

public class CpuCapacityRequirement implements CapacityRequirement {

	private final Fraction cpu;

	public CpuCapacityRequirement() {
		this(Fraction.ZERO);
	}
	
	public CpuCapacityRequirement(Fraction cpu) {
		this.cpu = cpu;
	}

	public CpuCapacityRequirement(double numberOfCpuCores) {
        this(convertCpuCoresFromDoubleToFraction(numberOfCpuCores));
    }

	public static Fraction convertCpuCoresFromDoubleToFraction(double cpu) {
        Fraction targetCpuCores;
        try {
            targetCpuCores = new Fraction(cpu);
        } catch (ConvergenceException e) {
            targetCpuCores = new Fraction((int)Math.ceil(cpu*2),2);
        }
        return targetCpuCores;
    }
	
    public Fraction getCpu() {
		return this.cpu;
	}

    public int compareTo(CapacityRequirement o) {
        return cpu.compareTo(cast(o).cpu);
    }

    public CpuCapacityRequirement multiply(int i) {
        return new CpuCapacityRequirement(cpu.multiply(i));
    }

    public CpuCapacityRequirement subtract(CapacityRequirement o) {
        if (!(o instanceof CpuCapacityRequirement)) {
            throw new IllegalArgumentException("Cannot subtract "  + o.getClass() + " from " + this.getClass());
        }
        Fraction otherCpu = cast(o).cpu;
        if (this.cpu.compareTo(otherCpu) < 0) {
            throw new IllegalArgumentException("Cannot subtract " + otherCpu + " from " + this.cpu + " since it would result in a negative number");  
        }
        return new CpuCapacityRequirement(this.cpu.subtract(otherCpu));
    }

    public CpuCapacityRequirement subtractOrZero(CapacityRequirement o) {
        Fraction otherCpu = cast(o).cpu;
        if (this.cpu.compareTo(otherCpu) < 0) {
            return new CpuCapacityRequirement();  
        }
        return new CpuCapacityRequirement(this.cpu.subtract(otherCpu));
    }

    public boolean equalsZero() {
        return cpu.equals(Fraction.ZERO);
    }

    public CpuCapacityRequirement add(CapacityRequirement o) {
        return new CpuCapacityRequirement(cpu.add(cast(o).cpu));
    }
    
    public String toString() {
        return cpu + " CPUs";
    }
    
    public boolean equals(Object otherRequirement) {
        return 
            otherRequirement instanceof CpuCapacityRequirement &&
            cpu.equals(((CpuCapacityRequirement)otherRequirement).cpu);
    }
    
    private CpuCapacityRequirement cast(CapacityRequirement o) {
        return (CpuCapacityRequirement)o;
    }

    public CapacityRequirement divide(int i) {
        if (i <= 0) {
            throw new IllegalArgumentException("i must be positive");
        }
        return new CpuCapacityRequirement(cpu.divide(i));
    }

    public CapacityRequirement min(CapacityRequirement otherCapacityRequirement) {
        Fraction otherCpu = ((CpuCapacityRequirement)otherCapacityRequirement).getCpu();
        return otherCpu.compareTo(cpu) < 0 ? otherCapacityRequirement : this;
    }

    public CapacityRequirement max(CapacityRequirement otherCapacityRequirement) {
        Fraction otherCpu = ((CpuCapacityRequirement)otherCapacityRequirement).getCpu();
        return otherCpu.compareTo(cpu) > 0 ? otherCapacityRequirement : this;
    }

    public double divide(CapacityRequirement otherCapacityRequirement) {
        Fraction otherCpu = ((CpuCapacityRequirement)otherCapacityRequirement).getCpu();
        return cpu.divide(otherCpu).doubleValue();
    }

    public CapacityRequirementType<CpuCapacityRequirement> getType() {
        return new CapacityRequirementType<CpuCapacityRequirement>(getClass());
    }

}
