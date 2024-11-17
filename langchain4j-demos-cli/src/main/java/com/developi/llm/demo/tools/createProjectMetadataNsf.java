package com.developi.llm.demo.tools;

import com.developi.jnx.utils.AbstractStandaloneJnxApp;
import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.Item;
import com.hcl.domino.html.HtmlConvertOption;
import com.hcl.domino.jnx.jsonb.DocumentJsonbSerializer;
import com.hcl.domino.mime.MimeReader;
import com.hcl.domino.mime.MimeWriter;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * This code creates an active copy of OpenNTF project metadata.
 * <p>
 * Usage example:
 * java -jar langchain4j-demos-cli.jar createProjectMetadataNsf \
 * -source=notesoss3/notesoss!!projects/pmt.nsf \
 * -target=maggie/developi!!demos/pmt_metadata.nsf \
 * -json=pmt_metadata.json
 * </p>
 */
public class createProjectMetadataNsf extends AbstractStandaloneJnxApp {

    DocumentJsonbSerializer serializer;
    long largestRtFieldSize = 0;
    private String sourceDbPath;
    private String targetDbPath;
    private String targetJsonFilePath;

    public static void main(String[] args) {
        new createProjectMetadataNsf().run(args);
    }

    @Override
    protected void _init() {
        String[] args = getArgs();

        // extract args. paramneters are case-insensitive
        for (String arg : args) {
            if (arg.toLowerCase(Locale.ENGLISH)
                   .startsWith("-source=")) {
                this.sourceDbPath = arg.substring("-source=".length());
            }

            if (arg.toLowerCase(Locale.ENGLISH)
                   .startsWith("-target=")) {
                this.targetDbPath = arg.substring("-target=".length());
            }

            if (arg.toLowerCase(Locale.ENGLISH)
                   .startsWith("-json=")) {
                this.targetJsonFilePath = arg.substring("-json=".length());
            }
        }

        if (StringUtils.isEmpty(this.sourceDbPath) && StringUtils.isEmpty(this.targetDbPath) && StringUtils.isEmpty(this.targetJsonFilePath)) {
            System.out.println("Usage:\n");
            System.out.println("java -jar langchain4j-demos-cli.jar createProjectMetadataNsf \\");
            System.out.println("\t\t-source=notesoss3/notesoss!!projects/pmt.nsf \\");
            System.out.println("\t\t-target=maggie/developi!!demos/pmt_metadata.nsf \\");
            System.out.println("\t\t-json=pmt_metadata.json\n");
            return;
        }

        this.serializer = DocumentJsonbSerializer.newBuilder()
                                                 .includeMetadata(false)
                                                 .booleanItemNames(List.of("released", "releaseStatus"))
                                                 .booleanTrueValues(List.of("y", "Y", "yes", "Yes", "true", "True"))
                                                 .excludeItems(List.of("$MIMETrack", "MIME_Version"))
                                                 .build();

    }

    @Override
    protected void _run(DominoClient dominoClient) {
        Database sourceDb;
        Database targetDb;
        File targetJsonFile = new File(targetJsonFilePath);

        sourceDb = dominoClient.openDatabase(sourceDbPath);
        System.out.println("Connected to source database: " + sourceDb.getTitle());

        targetDb = dominoClient.openDatabase(targetDbPath);
        System.out.println("Connected to target database: " + targetDb.getTitle());

        if (targetJsonFile.exists()) {
            System.out.println("The target json file already exists. Overwriting...");
        } else {
            System.out.println("Creating target json file: " + targetJsonFile.getAbsolutePath());
        }

        // Configure pretty printing for JsonGenerator
        Map<String, Object> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);

        JsonGeneratorFactory generatorFactory = Json.createGeneratorFactory(config);

        try (FileWriter fileWriter = new FileWriter(targetJsonFile); JsonGenerator jsonGenerator = generatorFactory.createGenerator(fileWriter)) {

            jsonGenerator.writeStartObject();
            jsonGenerator.writeStartArray("projects");

            importDocuments(TYPE.PROJECT, sourceDb, targetDb, jsonGenerator);

            jsonGenerator.writeEnd();

            jsonGenerator.writeStartArray("releases");
            importDocuments(TYPE.RELEASE, sourceDb, targetDb, jsonGenerator);
            jsonGenerator.writeEnd();

            jsonGenerator.writeEnd();
            System.out.println("JSON file written successfully to " + targetJsonFile.getAbsolutePath());
            System.out.println("The largest RT field size: " + this.largestRtFieldSize);

        } catch (IOException e) {
            throw new RuntimeException("Unable to work with json file!", e);
        }

    }

    private String getViewByType(TYPE type) {
        return switch (type) {
            case PROJECT -> "(ProjectList)";
            case RELEASE -> "ReleasesByDate";
        };
    }

    private void importDocuments(TYPE type, Database sourceDb, Database targetDb, JsonGenerator jsonGenerator) {
        String viewName = getViewByType(type);

        sourceDb.openCollection(viewName)
                .ifPresentOrElse(collection -> { // collection is not null
                    collection.forEachDocument(0, Integer.MAX_VALUE, (doc, loop) -> {
                        if (!doc.hasItem("$Conflict") && StringUtils.isNotEmpty(doc.getAsText("ProjectName", ' '))) {
                            String def = loop.getIndex() + ": " + doc.getAsText("ProjectName", ' ') + (type == TYPE.RELEASE ? ("." + doc.getAsText(
                                    "ReleaseNumber",
                                    ' '
                            )) : "");

                            System.out.println(def);

                            try {
                                Document metadataDoc = createMetaDataDoc(sourceDb, targetDb, doc);
                                serializer.serialize(metadataDoc, jsonGenerator, null);
                            } catch (DominoException e) {
                                throw new RuntimeException("Unable to create metadata doc with project (" + def + ")!", e);
                            }
                        }
                    });
                }, () -> { // collection is null
                    throw new RuntimeException("ProjectList collection not found!");
                });
    }

    private Document createMetaDataDoc(Database sourceDb, Database targetDb, Document doc) {

        // Remove existing ones
        targetDb.openCollection("(byId)")
                .ifPresent(collection -> collection.query()
                                                   .selectByKey(doc.getUNID(), true)
                                                   .forEachDocument(0, Integer.MAX_VALUE, (existingDoc, loop) -> existingDoc.delete(true)));

        Document metadataDoc = targetDb.createDocument();

        String form = doc.getAsText("Form", ' ');
        boolean isProject = "project".equalsIgnoreCase(form);

        String projectEncoded = URLEncoder.encode(doc.getAsText("ProjectName", ' '), StandardCharsets.UTF_8);

        metadataDoc.replaceItemValue("Form", isProject ? "project" : "release");
        metadataDoc.computeWithForm(false, null);

        metadataDoc.replaceItemValue("sourceDbPath", sourceDb.getRelativeFilePath());
        metadataDoc.replaceItemValue("sourceUnid", doc.getUNID());
        metadataDoc.replaceItemValue("sourceUrl", "https://www.openntf.org/main.nsf/project.xsp?r=project/" + projectEncoded);

        metadataDoc.replaceItemValue("id", doc.getUNID());

        if (isProject) {
            metadataDoc.replaceItemValue("name", doc.getAsText("ProjectName", ' '));
            metadataDoc.replaceItemValue("overview", doc.getAsText("ProjectOverview", ' '));

            copyRtField(doc, "Details", metadataDoc, "details");

            metadataDoc.replaceItemValue("downloads", doc.getAsInt("DownloadsProject", 0));
            metadataDoc.replaceItemValue("category", doc.getAsText("MainCat", ' '));
            metadataDoc.replaceItemValue("chefs", doc.getAsList("MasterChef", String.class, null));
            metadataDoc.replaceItemValue("cooks", doc.getAsList("ProjectCooks", String.class, null));
            metadataDoc.replaceItemValue("created", doc.get("Entry_Date", DominoDateTime.class, doc.getCreated()));
            metadataDoc.replaceItemValue("latestReleaseDate", doc.get("ReleaseDate", TemporalAccessor.class, doc.getLastModified()));
            metadataDoc.replaceItemValue("lastModified", doc.getLastModified());
            metadataDoc.replaceItemValue("sourceControlUrl", doc.getAsText("GithubProject", ' '));
        } else {
            // Release
            metadataDoc.replaceItemValue("projectName", doc.getAsText("ProjectName", ' '));
            metadataDoc.replaceItemValue("version", doc.getAsText("ReleaseNumber", ' '));
            metadataDoc.replaceItemValue("releaseDate", doc.get("ReleaseDate", TemporalAccessor.class, doc.getLastModified()));

            copyRtField(doc, "WhatsNew", metadataDoc, "description");

            metadataDoc.replaceItemValue("downloads", doc.getAsInt("DownloadsRelease", 0));
            metadataDoc.replaceItemValue("mainId", doc.getAsText("MainId", ' '));
            metadataDoc.replaceItemValue("releaseStatus", doc.getAsText("ReleaseInCatalog", ' '));
            metadataDoc.replaceItemValue("released", doc.getAsText("Status", ' '));
            metadataDoc.replaceItemValue("chef", doc.getAsText("Entry_Person", ' '));
            metadataDoc.replaceItemValue("masterChefs", doc.getAsList("MasterChef", String.class, null));
            metadataDoc.replaceItemValue("licenseType", doc.getAsText("LicenseType", ' '));
        }

        metadataDoc.save();
        return metadataDoc;
    }

    private void copyRtField(Document sourceDoc, String sourceField, Document targetDoc, String targetFieldPrefix) {
        DominoClient dc = sourceDoc.getParentDatabase()
                                   .getParentDominoClient();

        sourceDoc.getFirstItem(sourceField)
                 .ifPresent(item -> {

                     switch (item.getType()) {
                         case TYPE_COMPOSITE:
                             // Used DominoJNX implementation. To be improved.
                             String htmlContent = dc.getRichTextHtmlConverter()
                                                    .renderItem(sourceDoc, sourceField)
                                                    .option(HtmlConvertOption.XMLCompatibleHTML, "1")
                                                    .convert()
                                                    .getHtml();

                             if (htmlContent.length() > this.largestRtFieldSize) {
                                 this.largestRtFieldSize = htmlContent.length();
                             }

                             String text = Jsoup.parseBodyFragment(htmlContent)
                                                .text();
                             targetDoc.replaceItemValue(targetFieldPrefix + "Text", EnumSet.noneOf(Item.ItemFlag.class), text);

                             // Create MimeField
                             StringBuilder sb = new StringBuilder();
                             sb.append("MIME-Version: 1.0\n");
                             try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                                 MimeBodyPart bodyPart = new MimeBodyPart();
                                 bodyPart.setContent(htmlContent, "text/html");
                                 bodyPart.addHeader("Content-Type", "text/html; charset=UTF-8");
                                 bodyPart.writeTo(out);
                                 sb.append(out);

                                 dc.getMimeWriter()
                                   .writeMime(
                                           targetDoc,
                                           targetFieldPrefix,
                                           new ByteArrayInputStream(sb.toString()
                                                                      .getBytes(StandardCharsets.UTF_8)),
                                           EnumSet.of(MimeWriter.WriteMimeDataType.BODY)
                                   );

                             } catch (MessagingException | IOException e) {
                                 throw new RuntimeException(e);
                             }
                             break;

                         case TYPE_MIME_PART:
                             // Used DominoJNX implementation. To be improved.
                             MimeMessage mime = dc.getMimeReader()
                                                  .readMIME(sourceDoc, sourceField, EnumSet.of(MimeReader.ReadMimeDataType.MIMEHEADERS));

                             String mimeContent = "";

                             try {
                                 if (mime.getContent() instanceof MimeMultipart mimeMultipart) {

                                     System.out.println("Project " + sourceDoc.getAsText("ProjectName", ' ') + " has multiple mime parts!");
                                     for (int i = 0; i < mimeMultipart.getCount(); i++) {
                                         BodyPart part = mimeMultipart.getBodyPart(i);
                                         if (part.isMimeType("text/html")) {
                                             mimeContent = String.valueOf(part.getContent());
                                             break;
                                         }
                                     }
                                 } else {
                                     mimeContent = String.valueOf(mime.getContent());
                                 }

                                 String mimeContentText = Jsoup.parseBodyFragment(mimeContent)
                                                               .text();

                                 targetDoc.replaceItemValue(targetFieldPrefix + "Text", EnumSet.noneOf(Item.ItemFlag.class), mimeContentText);

                                 dc.getMimeWriter()
                                   .writeMime(
                                           targetDoc,
                                           targetFieldPrefix,
                                           mime,
                                           EnumSet.of(MimeWriter.WriteMimeDataType.BODY, MimeWriter.WriteMimeDataType.NO_DELETE_ATTACHMENTS)
                                   );

                             } catch (IOException | MessagingException e) {
                                 throw new RuntimeException(e);
                             }

                             break;
                         default:
                     }
                 });

    }

    enum TYPE {
        PROJECT, RELEASE
    }
}