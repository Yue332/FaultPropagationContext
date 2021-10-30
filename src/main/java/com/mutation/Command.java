package com.mutation;

import java.io.File;

public class Command {
	
	public static final String BASE_COMMAND = "java -cp @CLASS_PATH@:@DYNAMIC_PATH@:@srcClasses_Dir@:@testClasses_Dir@ @EXE_MODULE@ "
			+ "--fullMutationMatrix true "
			+ "--outputFormats XML "  
			+ "--reportDir @reports_Dir@ "
			+ "--targetClasses @targetClasses@ "
			+ "--targetTests @targetTests@ "
			+ "--sourceDirs @source_Dir@ "
			+ "--verbose true";
	
	public static final String COMMAND = "sed -i 's/\\r$//' @MUTATION_FILE@";
	
	public static final String LOAD_CLASSES_PATH = "@D4J_HOME@" + File.separator + "framework" + File.separator + "projects" + File.separator + 
			"@PROJECT_ID@" + File.separator + "loaded_classes" + File.separator + "@BUG_ID@.src";
	
	public static final String TARGET_TESTS_PATH = "@D4J_HOME@" + File.separator + "framework" + File.separator + 
			"projects" + File.separator + "@PROJECT_ID@" + File.separator + "relevant_tests" + File.separator + "@BUG_ID@";
	
	public static final String PRO_KEY_CLASS_PATH = "CLASS_PATH";
	
	public static final String PRO_KEY_EXE_MODULE = "EXE_MODULE";
	
	public static final String PRO_MUTATION_PROJECT_PATH = "MUTATION_PROJECT_PATH";
	
	public static final String DEFAULT_EXE_MODULE = "org.pitest.mutationtest.commandline.MutationCoverageReport";
}
