package ru.otus.geolocation.service;

import com.sun.mail.util.MailSSLSocketFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.geolocation.domain.IncomingMailSetting;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.Properties;

/**
 * Класс для работы с почтой.
 * @author Evgeniy Kuchumov
 */
@Slf4j
@AllArgsConstructor
@Service
public class MailService {

    private final IncomingMailSettingService incomingMailSettingService;

    public byte[] downloadEmailAttachment() {
        Optional<IncomingMailSetting> incomingMailSetting = incomingMailSettingService.getSet();
        if (!incomingMailSetting.isPresent()) {
            log.error("Настройки входящей почты не найдены");
            return new byte[]{};
        }
        log.info("Подключаемся к {} для поиска писем с файлом адресов точек продаж", incomingMailSetting.get().getUser());
        Properties properties = createInProperties(incomingMailSetting.get().getHost(), incomingMailSetting.get().getPort());
        Session session = Session.getDefaultInstance(properties);
        try (Store store = session.getStore("imap")) {
            store.connect(incomingMailSetting.get().getUser(), incomingMailSetting.get().getPassword());
            try (Folder folderInbox = store.getFolder("INBOX")) {
                folderInbox.open(Folder.READ_WRITE);
                Message[] arrayMessages = folderInbox.getMessages();
                for (Message message : arrayMessages) {
                    if (message.getContentType().contains("multipart")) {
                        Multipart multiPart = (Multipart) message.getContent();
                        int numberOfParts = multiPart.getCount();
                        for (int partCount = 0; partCount < numberOfParts; partCount++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                                String pattern = "sale-addresses.*\\.xlsx";
                                if (part.getFileName().matches(pattern)) {
                                    log.info("Найден файл {}", part.getFileName());
                                    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                                         InputStream in = part.getInputStream()) {
                                        byte[] buf = new byte[8192];
                                        int len;
                                        while ((len = in.read(buf)) > 0) {
                                            out.write(buf, 0, len);
                                        }
                                        folderInbox.copyMessages(new Message[]{message}, store.getFolder("Done"));
                                        message.setFlag(Flags.Flag.DELETED, true);
                                        return out.toByteArray();
                                    }
                                } else {
                                    log.error("Имя вложенного файла {} не совпадает с установленным шаблоном", part.getFileName());
                                }
                            }
                        }
                    }
                }
            }
        } catch (MessagingException | IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        log.info("Письма с искомыми файлами адресов не найдены");
        return new byte[]{};
    }

    private Properties createInProperties(final String host, final String port) {
        final Properties properties = new Properties();
        final MailSSLSocketFactory sf;
        try {
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            properties.put("mail.imap.host", host);
            properties.put("mail.imap.port", port);
            properties.put("mail.imap.socketFactory", sf);
            properties.put("mail.*.ssl.trust", "imap");
            properties.put("mail.imap.partialfetch", "false");
            properties.put("mail.imap.fetchsize", "1048576");
            properties.put("mail.imaps.partialfetch", "false");
            properties.put("mail.imaps.fetchsize", "1048576");
            properties.setProperty("mail.imap.socketFactory.fallback", "false");
            properties.setProperty("mail.imap.socketFactory.port", port);
        } catch (GeneralSecurityException e) {
            log.error("Ошибка доступа при создании пропертис для чтения писем: {}, {}", e.getMessage(), e);
        }
        return properties;
    }
}
