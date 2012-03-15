package org.eclipse.linuxtools.internal.lttng.jni.common;

public class Jni_C_Pointer_And_Library_Id extends Jni_C_Pointer {
	
	// *** Library Id
	// Needed to know which library we need to make our functions call in case several are loaded
	// This make possible to use several different trace version at the same time
	private int libraryId = -1;
	
	/**
     * Default constructor.<p>
     * 
     * Note : Pointer will be set to a 64bits "NULL" and Id -1.
     */
    public Jni_C_Pointer_And_Library_Id() {
        super();
        libraryId = -1;
    }
    
    /**
     * Constructor with parameters for 64bits pointers and library handle id.
     * 
     * @param newPtr    long-converted (64 bits) C pointer.
     * @param newHandle a valid library id as int 
     */
    public Jni_C_Pointer_And_Library_Id(int newId, long newPtr) {
        super(newPtr);
        libraryId = newId;
    }
    
    /**
     * Constructor with parameters for 32bits pointers and library handle id.
     * 
     * @param newPtr    int-converted (32 bits) C pointer.
     * @param newHandle a valid library id as int 
     */
    public Jni_C_Pointer_And_Library_Id(int newId, int newPtr) {
    	super(newPtr);
    	libraryId = newId;
    }
    
    /**
     * Copy constructor.<p>
     * 
     * @param oldPointerAndId	The old object to copy from.
     */
    public Jni_C_Pointer_And_Library_Id(Jni_C_Pointer_And_Library_Id oldPointerAndId) {
    	super(oldPointerAndId.ptr);
    	libraryId = oldPointerAndId.libraryId;
    }
    
    /**
     * Get the library handle id currently in use.<p>
     * Id is used to tell the C which version of the functions to call.<p>
     * 
     * @return The current id
     */
    public int getLibraryId() {
		return libraryId;
	}
    
    /**
     * Set a new library id.<p>
     * Id is used to tell the C which version of the functions to call.<p>
     * 
     * @param newHandleId	The new Id to use (must be a valid id for the C library).
     */
	public void setLibraryId(int newId) {
		this.libraryId = newId;
	}
}
