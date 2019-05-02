package com.gy.businessCore.common;

/**
 * Created by gy on 2018/6/8.
 */
public interface BusinessEnum {

    enum MonitorRecordEnum {
        SNMP_VERSION_V1("snmp_v1"),
        SNMP_VERSION_V2("snmp_v2c");


        private String value;

        MonitorRecordEnum(String msg) {
            this.value = msg;
        }

        public String value() {
            return this.value;
        }
    }
    enum MonitorTypeEnum{
        SNMP("snmp"),

        MYSQL("mysql"),

        TOMCAT("tomcat"),

        CAS("cas"),

        CASCLUSTER("cascluster"),

        CVK("cas_cvk"),

        VIRTUALMACHINE("cas_vm"),

        K8S("k8s"),

        K8SNODE("k8sn"),

        K8SCONTAINER("k8sc");

        private String value;

        MonitorTypeEnum(String msg) {
            this.value = msg;
        }

        public String value() {
            return this.value;
        }
    }

    enum LightTypeEnum{

        MYSQL("MySQL"),

        TOMCAT("Tomcat"),

        CAS("CAS"),

        CASCLUSTER("CASCluster"),

        CVK("CVK"),

        VIRTUALMACHINE("VirtualMachine"),

        K8S("k8s"),

        K8SNODE("k8sNode"),

        K8SCONTAINER("k8sContainer");

        private String value;

        LightTypeEnum(String msg) {
            this.value = msg;
        }

        public String value() {
            return this.value;
        }
    }

    enum WeaveEnum {
        IMAGE_TABLE("image_table"),
        IMAGE_ID("label_ ID"),
        IMAGE_NAME("label_ Name"),
        IMAGE_TAG("label_ Tag");


        private String value;

        WeaveEnum(String msg) {
            this.value = msg;
        }

        public String value() {
            return this.value;
        }
    }

    enum BusyQuotaEnum {
        PROCESSING_PERSEC("processingPersec"),
        THREAD_BUSY_PERCENT("threadBusyPercent"),
        MYSQL_QUESTIONS_RATE("mysql_questions_rate"),
        CPU_PERCENT("cpu_percent"),
        MEMORY_PERCENT("memory_percent");


        private String value;

        BusyQuotaEnum(String msg) {
            this.value = msg;
        }

        public String value() {
            return this.value;
        }
    }


    enum BusyWeightTypeEnum{
        MYSQL("mysql"),

        TOMCAT("tomcat"),

        CVK("cvk"),

        VIRTUALMACHINE("virtualMachine"),

        K8SNODE("k8snode"),

        K8SCONTAINER("k8scontainer");

        private String value;

        BusyWeightTypeEnum(String msg) {
            this.value = msg;
        }

        public String value() {
            return this.value;
        }
    }
    enum VariableWeightParamEnum {
        VARIABLE_A("variable.a"),
        VARIABLE_B("variable.b");

        private String value;

        VariableWeightParamEnum(String msg) {
            this.value = msg;
        }

        public String value() {
            return this.value;
        }
    }

    enum ResourceWeightEnum {
        SITUATION_ONE("situationOne"),
        SITUATION_WITH_MYSQL("situationWithMySQL"),
        SITUATION__WITH_TOMCAT("situationWithTomcat"),
        SITUATION_WITH_BOTH("situationWithBoth");

        private String value;

        ResourceWeightEnum(String msg) {
            this.value = msg;
        }

        public String value() {
            return this.value;
        }
    }

    enum QuotaEnum {
        TOMCAT_PROCESSINGPERSEC("monitor.tomcat.Tomcat_GlobalRequestProcessor_processingPersec"),
        TOMCAT_THREADSBUSYPERCENT("monitor.tomcat.Tomcat_ThreadPool_ThreadsBusyPercent"),
        MYSQL_QUESTIONSRATE("monitor.mysql.mysql_questions_rate"),
        CVK_CPU_USAGE("monitor.cvk.cas_cvk_host_cpu_usage"),
        CVK_MEM_USAGE("monitor.cvk.cas_cvk_host_memory_usage"),
        VM_CPU_USAGE("monitor.vm.cas_vm_cpu_usage"),
        VM_MEM_USAGE("monitor.vm.cas_vm_memory_usage"),
        K8SNODE_CPU_USAGE("monitor.k8snode.k8s_node_cpu_usage"),
        K8SNODE_MEM_USAGE("monitor.k8snode.k8s_node_memory_usage"),
        K8SCONTAINER_CPU_USAGE("monitor.k8scontainer.k8s_container_cpu_usage"),
        K8SCONTAINER_MEM_USAGE("monitor.k8scontainer.k8s_container_memory_usage"),
        MYSQL_MONITORSTATUS("mysql_monitorstatus"),
        TOMCAT_MONITORSTATUS("jmx_monitorstatus"),
        CVK_MONITORSTATUS("cas_cvk_monitorstatus"),
        VIRTUALMACHINE_MONITORSTATUS("cas_vm_monitorstatus"),
        K8SCONTAINER_MONITORSTATUS("k8s_container_monitorstatus"),
        K8SNODE_MONITORSTATUS("k8s_node_monitorstatus");




        private String value;

        QuotaEnum(String msg) {
            this.value = msg;
        }

        public String value() {
            return this.value;
        }
    }

    enum AlertTypeEnum {
        CRITICAL("critical"),//紧急
        MAJOR("major"),//重要
        MINOR("minor"),//次要
        WARNING("warning"),//警告
        NOTICE("notice");//通知


        private String value;

        AlertTypeEnum(String msg) {
            this.value = msg;
        }

        public String value() {
            return this.value;
        }
    }
    enum BusinessAvailableEnum {
        AVAILABLE_INTERVAL("business.available.interval");


        private String value;

        BusinessAvailableEnum(String msg) {
            this.value = msg;
        }

        public String value() {
            return this.value;
        }
    }
}
