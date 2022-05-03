package com.module.intellifl;

import com.module.IProcessModule;
import com.utils.Bean;
import com.utils.Configer;
import com.utils.Utils;

public class IntelliFLCallGraph extends Bean implements IProcessModule {

    private String call_graph_command = "java -cp /home/yy/intelliFL/intelliFL.jar set.intelliFL.cg.CallGraphBuilder @:BUILD@ \r\n";

    public IntelliFLCallGraph(Configer config) {
        super(config);
    }

    @Override
    public void process(Runtime runTime) throws Exception {
        String buildPath = Utils.getSrcDir(runTime, projectPath);

    }

    @Override
    public void onPrepare() {

    }
}
