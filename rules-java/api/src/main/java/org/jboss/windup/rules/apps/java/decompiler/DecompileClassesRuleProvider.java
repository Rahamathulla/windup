package org.jboss.windup.rules.apps.java.decompiler;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.DecompilationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This will decompile all Java .class files found in the incoming application.
 *
 * This will use the Fernflower decompiler by default, however this can be overridden with a system property (
 * {@link DecompileClassesRuleProvider#DECOMPILER_PROPERTY}).
 *
 */
public class DecompileClassesRuleProvider extends AbstractRuleProvider
{
    /**
     * This System Property can be set to either {@link DecompilerType#PROCYON} or {@link DecompilerType#FERNFLOWER} to manually select the decompiler
     * to use during Windup execution.
     */
    public static final String DECOMPILER_PROPERTY = "windup.decompiler";

    private enum DecompilerType
    {
        PROCYON,
        FERNFLOWER
    }

    public DecompileClassesRuleProvider()
    {
        super(MetadataBuilder.forProvider(DecompileClassesRuleProvider.class)
                    .setPhase(DecompilationPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
        .addRule()
        .perform(new DecompileCondition());
    }
    // @formatter:on

    private class DecompileCondition extends GraphOperation
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            switch (getDecompilerType())
            {
            case FERNFLOWER:
                new FernflowerDecompilerOperation().perform(event, context);
                break;
            case PROCYON:
                new ProcyonDecompilerOperation().perform(event, context);
                break;
            default:
                throw new WindupException("Failed to select decompiler due to unrecognized type: " + getDecompilerType());
            }
        }

        private DecompilerType getDecompilerType()
        {
            String decompilerProperty = System.getProperty(DECOMPILER_PROPERTY);
            if (StringUtils.isBlank(decompilerProperty))
                return DecompilerType.FERNFLOWER;
            else
            {
                return DecompilerType.valueOf(decompilerProperty.toUpperCase());
            }
        }
    }
}