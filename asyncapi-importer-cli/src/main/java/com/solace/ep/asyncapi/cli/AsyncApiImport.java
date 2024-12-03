/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.solace.ep.asyncapi.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.solace.ep.asyncapi.importer.AsyncApiImporter;

public class AsyncApiImport {

    private static final String CMD_LINE_SYNTAX = "asyncapi-import -a ASYNCAPI_TO_IMPORT -d APP_DOMAIN -t EP_TOKEN [-u BASE_URL] [-m | -i | -p]";

    public static void main(String[] args) 
    {
        // Define Options
        Option opAsyncapi = new Option("a", "asyncapi", true, "AsyncApi Spec File to import");
        Option opAppDomain = new Option("d", "app-domain", true, "Target Application Domain in Event Portal for Import");
        Option opEpToken = new Option("t", "ep-token", true, "Event Portal bearer token");
        Option opBaseUrl = new Option("u", "ep-base-url", true, "Base URL to call Event Portal\nUse to call Solace cloud API\noutside of US/CAN region");
        Option opHelp = new Option("h", "help", false, "Display Help");

        Option opVersionMajor = new Option("m", "version-major", false, "Increment MAJOR version of SemVer for new objects (DEFAULT)");
        Option opVersionMinor = new Option("i", "version-minor", false, "Increment MINOR version of SemVer for new objects");
        Option opVersionPatch = new Option("p", "version-patch", false, "Increment PATCH version of SemVer for new objects");

        opAsyncapi.setRequired(true);
        opAppDomain.setRequired(true);
        opEpToken.setRequired(true);

        OptionGroup opGroupVersion = new OptionGroup();
        opGroupVersion.addOption(opVersionMajor).addOption(opVersionMinor).addOption(opVersionPatch);

        Options options = new Options();
        options
            .addOption(opAsyncapi)
            .addOption(opAppDomain)
            .addOption(opEpToken)
            .addOption(opBaseUrl)
            .addOptionGroup(opGroupVersion)
            .addOption(opHelp);

        // Collect Option values
        String appDomainName;
        String asyncapiSpecFile;
        String epToken;
        String epBaseUrl;
        String versionStrategy;
        
        // Parse out options
        try {
            if (checkForHelpOption(options, args)) {
                return;
            }
            CommandLineParser cliParser = new DefaultParser();
            CommandLine commandLine = cliParser.parse(options, args);

            appDomainName = commandLine.getOptionValue("d");
            asyncapiSpecFile = commandLine.getOptionValue("a");
            epToken = commandLine.getOptionValue("t");
            epBaseUrl = commandLine.hasOption("u") ? commandLine.getOptionValue("u") : null;
            if (commandLine.hasOption(opGroupVersion)) {
                if (commandLine.hasOption("m")) {
                    versionStrategy = "MAJOR";
                } else if (commandLine.hasOption("i")) {
                    versionStrategy = "MINOR";
                } else if (commandLine.hasOption("p")) {
                    versionStrategy = "PATCH";
                } else {
                    versionStrategy = null;
                }
            } else {
                versionStrategy = null;
            }
        } catch (ParseException parseExc) {
            System.out.println("Error parsing out options: " + parseExc.getLocalizedMessage() + "\n");
            displayHelp(options);
            return;
        }

        System.out.println(
            "\n####  STARTING ASYNCAPI SPEC IMPORT TO EVENT PORTAL  ####\n"
        );
        System.out.println(
            String.format("App Domain: %s\nAsyncapi spec: %s\nBase URL: %s\nVersion Strategy: %s\n",
                appDomainName, asyncapiSpecFile, (epBaseUrl == null ? "[Default]" : epBaseUrl), (versionStrategy == null ? "[Default]" : versionStrategy) )
        );

        try {
            final String asyncApiContent = getFileAsString(asyncapiSpecFile);

            AsyncApiImporter.execImportOperation(appDomainName, epToken, asyncApiContent, epBaseUrl, versionStrategy);
            
            System.out.println(
                "\n####  ASYNCAPI SPEC IMPORT COMPLETE  ####\n"
            );
        } catch (Exception exc) {
            System.out.println("\n////  ASYNCAPI SPEC IMPORT FAILED WITH AN ERROR  ////\n////  Error Message: " + exc.getMessage() + "\n");
        }
    }

    private static boolean checkForHelpOption( final Options options, final String[] args ) throws ParseException
    {
        CommandLineParser cliParser = new DefaultParser();
        CommandLine commandLine = cliParser.parse(options, args);
        boolean hasHelpOption = commandLine.hasOption("h");
        if (hasHelpOption) {
            displayHelp(options);
        }
        return hasHelpOption;
    }

    private static void displayHelp( final Options options )
    {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(CMD_LINE_SYNTAX, options);
    }

    public static String getFileAsString(final String fileName) throws Exception 
    {
        Path path = Paths.get( fileName );
        try {
            String content = Files.readString(path);
            return content;
        } catch (IOException exc) {
            System.out.println("Error reading input file: [" + fileName + "]");
            System.out.println(exc.getLocalizedMessage());
            throw exc;
        }
    }
}