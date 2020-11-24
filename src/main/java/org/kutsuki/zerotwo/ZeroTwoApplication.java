package org.kutsuki.zerotwo;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZeroTwoApplication {
    private static final String GIF = "/ZeroTwo.gif";
    private static final String TITLE = "Zero Two";

    public static void main(String[] args) {
	ConfigurableApplicationContext ctx = new SpringApplicationBuilder(ZeroTwoApplication.class)
		.headless(false).run(args);

	URL url = ZeroTwoApplication.class.getResource(GIF);
	Icon icon = new ImageIcon(url);
	JLabel label = new JLabel(icon);

	JFrame f = new JFrame(TITLE);
	f.getContentPane().add(label);
	f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	f.pack();
	f.setLocationRelativeTo(null);
	f.setResizable(false);
	f.setVisible(true);
	f.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent windowEvent) {
		SpringApplication.exit(ctx, new ExitCodeGenerator() {
		    @Override
		    public int getExitCode() {
			return 0;
		    }
		});

		f.setVisible(false);
		f.dispose();
	    }
	});
    }
}
