package monitor.metrics.common;

import com.sohu.idcenter.IdWorker;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetAddress;


public class IdGenerator implements InitializingBean {

    private String group;

    private static final IdGenerator IDGENERATOR = new IdGenerator();

    private IdWorker idWorker;

    public void setGroup(String aGroup) {
        this.group = aGroup;
    }

    public static String next() {
        return "" + IDGENERATOR.idWorker.getId();
    }

    public void afterPropertiesSet() throws Exception {
        String id = InetAddress.getLocalHost().getHostAddress() + ":" + group;
        long idepo = System.identityHashCode(id);
        IDGENERATOR.idWorker = new IdWorker(idepo);
    }

/*	public static void main(String[] args) throws Exception{
		String name= InetAddress.getLocalHost().getHostAddress();
		idGenerator.idWorker = new IdWorker(1);

		System.out.println(idGenerator.idWorker.getId());
	}*/
}
