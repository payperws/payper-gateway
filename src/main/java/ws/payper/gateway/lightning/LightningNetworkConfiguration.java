package ws.payper.gateway.lightning;


import org.lightningj.lnd.wrapper.ClientSideException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import ws.payper.gateway.hedera.NetworkCommunicationException;

import javax.net.ssl.SSLException;
import java.io.File;

@Configuration
@Lazy
@PropertySource("classpath:lightning-conn.properties")
public class LightningNetworkConfiguration  {

    @Value("${rpcserver.host}")
    private String host;

    @Value("${rpcserver.port}")
    private int port;

    @Value("${tlscert.path}")
    private File tlsCert;

    @Value("${macaroon.path}")
    private File macaroon;

    @Bean
    public SynchronousLndAPI synchronousLndAPI() {
        try {
            return new SynchronousLndAPI(host, port, tlsCert, macaroon);
        } catch (SSLException | ClientSideException e) {
            throw new NetworkCommunicationException(e);
        }
    }

}
