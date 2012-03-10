/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.model.trange;

public class TimeRangeEventProcess extends TimeRangeComposite implements
		Comparable<TimeRangeEventProcess> {
	// ========================================================================
	// Data
	// =======================================================================
	// GUI information
	private Long pid = 0L;
	private Long tgid = 0L;
	private Long ppid = 0L;
	private Long creationTime = 0L;
	private String traceID = ""; //$NON-NLS-1$
	private String processType = "User"; // Kernel or user thread //$NON-NLS-1$
	private Long cpu = 0L;
	private String brand = ""; //$NON-NLS-1$

	// ========================================================================
	// Constructor
	// =======================================================================
	/**
	 * @param id
	 * @param name
	 * @param sTime
	 *            normally set to the Trace start time
	 * @param stopTime
	 *            normally set to the Trace end time
	 * @param groupName
	 * @param className
	 */
	public TimeRangeEventProcess(int id, String name, long startTime,
			long stopTime, String groupName, String className, Long cpu,
			long insertionTime) {

		super(id, startTime, stopTime, name, CompositeType.PROCESS,
				insertionTime);
		this.cpu = cpu;
	}

	// ========================================================================
	// Methods
	// =======================================================================
	
	
	/**
     * Interface to add children to this process
     * 
     * @param newEvent
     */
    public void addChildren(TimeRangeEvent newEvent) {
        if ((newEvent != null)) {
            this.ChildEventLeafs.add(newEvent);
        }
    }
	
	/**
	 * @return
	 */
	public Long getPid() {
		return pid;
	}

	/**
	 * @param pid
	 */
	public void setPid(Long pid) {
		this.pid = pid;
	}

	/**
	 * @return
	 */
	public Long getTgid() {
		return tgid;
	}

	/**
	 * @param tgid
	 */
	public void setTgid(Long tgid) {
		this.tgid = tgid;
	}

	/**
	 * @return
	 */
	public Long getPpid() {
		return ppid;
	}

	/**
	 * @param ppid
	 */
	public void setPpid(Long ppid) {
		this.ppid = ppid;
	}

	/**
	 * @return
	 */
	public Long getCreationTime() {
		return creationTime;
	}

	/**
	 * @param creationTime
	 */
	public void setCreationTime(Long creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * @return
	 */
	public String getTraceID() {
		return traceID;
	}

	/**
	 * @param traceID
	 */
	public void setTraceID(String traceID) {
		if (traceID != null) {
			this.traceID = traceID;
		} else {
			this.traceID = ""; //$NON-NLS-1$
		}
	}

	/**
	 * @return
	 */
	public String getProcessType() {
		return processType;
	}

	/**
	 * @param processType
	 */
	public void setProcessType(String processType) {
		if (processType != null) {
			this.processType = processType;
		}
	}

	/**
	 * @return
	 */
	public Long getCpu() {
		return cpu;
	}

	/**
	 * @param cpu
	 */
	public void setCpu(Long cpu) {
		if (cpu != null) {
			this.cpu = cpu;
		} else {
		    this.cpu = 0L;
		}
	}

	/**
	 * @return
	 */
	public String getBrand() {
        return brand;
    }

	/**
	 * @param brand
	 */
    public void setBrand(String brand) {
        if (brand != null) {
            this.brand = brand;
        } else {
            brand = ""; //$NON-NLS-1$
        }
    }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TimeRangeEventProcess process) {
		if (process != null) {
			int result = 0;
			// first compare by pid
			Long anotherPid = process.getPid();
			result = pid.compareTo(anotherPid);
			if (result != 0) {
				return result;
			}

			// Then by CPU
			Long anotherCpu = process.getCpu();
			result = cpu.compareTo(anotherCpu);
			if (result != 0) {
				return result;
			}

			// finally by trace
			String anotherTraceId = process.getTraceID();
			return traceID.compareTo(anotherTraceId);
		}

		return 0;
	}
	
    @Override
    @SuppressWarnings("nls")
    public String toString() {
		return "[TimeRangeEventProcess:" + super.toString() +
		",pid=" + pid + ",tgid=" + tgid + ",ppid=" + ppid + ",ctime=" + creationTime +
		",trace=" + traceID + ",ptype=" + processType + ",cpu=" + cpu + ",brand=" + brand + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((brand == null) ? 0 : brand.hashCode());
        result = prime * result + ((cpu == null) ? 0 : cpu.hashCode());
        result = prime * result + ((creationTime == null) ? 0 : creationTime.hashCode());
        result = prime * result + ((pid == null) ? 0 : pid.hashCode());
        result = prime * result + ((ppid == null) ? 0 : ppid.hashCode());
        result = prime * result + ((processType == null) ? 0 : processType.hashCode());
        result = prime * result + ((tgid == null) ? 0 : tgid.hashCode());
        result = prime * result + ((traceID == null) ? 0 : traceID.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TimeRangeEventProcess)) {
            return false;
        }
        TimeRangeEventProcess other = (TimeRangeEventProcess) obj;
        if (brand == null) {
            if (other.brand != null) {
                return false;
            }
        } else if (!brand.equals(other.brand)) {
            return false;
        }
        if (cpu == null) {
            if (other.cpu != null) {
                return false;
            }
        } else if (!cpu.equals(other.cpu)) {
            return false;
        }
        if (creationTime == null) {
            if (other.creationTime != null) {
                return false;
            }
        } else if (!creationTime.equals(other.creationTime)) {
            return false;
        }
        if (pid == null) {
            if (other.pid != null) {
                return false;
            }
        } else if (!pid.equals(other.pid)) {
            return false;
        }
        if (ppid == null) {
            if (other.ppid != null) {
                return false;
            }
        } else if (!ppid.equals(other.ppid)) {
            return false;
        }
        if (processType == null) {
            if (other.processType != null) {
                return false;
            }
        } else if (!processType.equals(other.processType)) {
            return false;
        }
        if (tgid == null) {
            if (other.tgid != null) {
                return false;
            }
        } else if (!tgid.equals(other.tgid)) {
            return false;
        }
        if (traceID == null) {
            if (other.traceID != null) {
                return false;
            }
        } else if (!traceID.equals(other.traceID)) {
            return false;
        }
        return true;
    }

}
