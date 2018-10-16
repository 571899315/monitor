package monitor.metrics.collector.jvm;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


class MBeanNode implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;

    private final String description;

    private final List<MBeanNode> children;

    private final List<MBeanAttribute> attributes;

    static class MBeanAttribute implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String name;

        private final String description;

        private final String formattedValue;

        MBeanAttribute(String aName, String aDescription, String aFormattedValue) {
            super();
            this.name = aName;
            this.description = aDescription;
            this.formattedValue = aFormattedValue;
        }

        String getName() {
            return name;
        }

        String getDescription() {
            return description;
        }

        String getFormattedValue() {
            return formattedValue;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[name=" + getName() + ", formattedValue="
                    + getFormattedValue() + ']';
        }
    }

    MBeanNode(String aName) {
        super();
        this.name = aName;
        this.description = null;
        this.children = new ArrayList<MBeanNode>();
        this.attributes = null;
    }

    MBeanNode(String aName, String aDescription, List<MBeanAttribute> aAttributes) {
        super();
        this.name = aName;
        this.description = aDescription;
        this.children = null;
        this.attributes = aAttributes;
    }

    String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    List<MBeanNode> getChildren() {
        return children != null ? children : null;
    }

    List<MBeanAttribute> getAttributes() {
        return attributes != null ? attributes : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + getName() + ']';
    }
}
