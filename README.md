# Property Manager

When you deploy web assets to the Akamai network, you create rules that tell Akamai edge servers how to process end-user requests for your content. Akamai is always seeking ways to make managing complex configurations seamless and quick. With the **Property Manager** plugin, you can edit and validate Property Manager API (PAPI) JSON rule trees the same way you interact with other parts of your infrastructure. Once you've updated and validated the configuration file, use [PAPI](https://learn.akamai.com/en-us/products/core_features/property_manager.html) or [Property Manager CLI](https://developer.akamai.com/cli/packages/property-manager.html) to push the updated file back to the Akamai platform.

## Features

- Editing rules, adding behaviors and criteria
- Syntax suggestions based on your Akamai product
- Inline JSON syntax checker
- Property Manager variables support
- Rule tree validation
- Error handling with links to code lines

## Before you begin

To benefit from all features supported by Property Manager, first install the **Eclipse Web Developer Tools** plugin.

Before you use the Property Manager plugin, make sure you have access to [Control Center](https://control.akamai.com/) with the appropriate roles and permissions to create API clients and manage credentials. An API client contains authentication tokens that secure the interactions between your application and the Akamai platform. Contact an Akamai administrator within your company and ask them to create the API credentials for you if you don’t have either of these permissions in your role:

- IDM: API Clients – User Access
- IDM: API Clients – Admin Access

### Create API credentials

With admin access to Akamai Control Center, you can configure your own tokens and client secrets.

1. Launch [Identity and Access Management](https://control.akamai.com/apps/identity-management/). In Akamai Control Center, click <span style="font-size:large;font-weight:bold">&Congruent;</span> &rArr; ACCOUNT ADMIN &rArr; Identity & access.
2. From the **Users and API Clients** tab, click **New API client for me** to open the *Customize API client* screen.
3. Follow one of the scenarios:
    a. To instantly create an API client and credentials for the Akamai APIs you can access, click **Quick**. This client’s API access levels, group roles, and permissions are identical to yours. For details about roles, permissions, and access levels, see [Identity and Access Management](https://control.akamai.com/dl/IDM/IAM/index.html).
    b. To make updates to one or more of the accounts you manage, click **Advanced**. In the *Details* section, select **Let this client manage multiple accounts** . For details, see [Manage multiple accounts with one API client](https://control.akamai.com/wh/CUSTOMER/AKAMAI/en-US/WEBHELP/identity-management/idm-help/GUID-D05CDFA1-CFCB-4D70-9CDD-F1933C27883F.html).

The client’s name, description, and notification list populate for you in the *Details* section. You can change this information at any time. The set of credentials that appears in the *Credentials* section includes the client token and client secret needed to authenticate the extension.

4. Check that you have `READ-WRITE` access to the Property Manager API:
    a. Under *Details*, click **Show additional details**.
    b. Scroll through the APIs for Property Manager.
    c. If the API isn’t listed, contact your account representative for assistance.
5. Click **Download**, then add the credentials to the `.edgerc` file.

### Add credentials to the .edgerc file

Configure the EdgeGrid credentials file that includes client tokens and client secrets for the Akamai accounts you manage. You’ll need this file to authenticate the connection between the extension and the Akamai platform.

1. Open the file you downloaded in a text editor.
2. Add a line above the credentials for your account as follows: `[default]`.

    ```
    [default]
    client_secret = aaaaaaaaaaaaaaaaaaaa12345xyz=
    host = XXXXXXXXXXXXXXXX-XXXXXXXXXXXXXXXX.luna.akamaiapis.net
    access_token = akaa-xxxxxxxxxxxxxxxx-xxxxxxxxxxxxxxxx
    client_token = akaa-xxxxxxxxxxxxxxxx-xxxxxxxxxxxxxxxx

    [credentialset1]
    client_secret = xxxxxxxxxxxxxxxxxxxx1234567890=
    host = YYYYYYYYYYYYYYYY-YYYYYYYYYYYYYYYY.luna.akamaiapis.net
    access_token = akaa-xxxxxxxxxxxxxxxx-xxxxxxxxxxxxxxxx
    client_token = akaa-xxxxxxxxxxxxxxxx-xxxxxxxxxxxxxxxx
    ```

    > **NOTE**: You can add other credentials to this file as needed. Separate each set of credentials with a `[header]` as shown in the example. Then, while setting up the authentication in VS Code, you can select the credentials you want to use for editing.

3. Save the file in your home directory with the name `.edgerc`.

## Get started

1. Install the **Property Manager** plugin from the Eclipse Marketplace.
2. When you run the plugin for the first time, you need to set up your credentials:
   a. From the **Property Manager** menu, select **Set Credentials**.
   b. Upload the `.edgerc` file. For MacOS, press **Command+Shift+Dot** to show the hidden files.
   c. From the dropdown, select your **EdgeGrid credentials** that provide access to the properties you want to edit.
   d. **Optional:** If you created an API client that can manage multiple accounts, enter the **Account Switch Key**.
   e. Click **Save**.

> **NOTE**: The plugin verifies your client token against the property’s group and contract. You can only edit and validate configuration files you have permissions to.

To start editing a JSON configuration file, you can either:

- Download a rules file from the platform.
    a. From the **Property Manager** menu, select **Download Rule Tree**.
    b. Enter an existing property name.
    c. Once the name is verified, from the drop-down select the property version. The list shows the latest property version and, if applicable, the versions activated on staging or production networks. To create a new version, you can use an active one as a baseline.
    d. Click **Download**.
- Use a local rules file. Open a local file in Eclipse and from the **Property Manager** menu, select **Validate Rule Tree**. This checks your configuration and enables the plugin features for further editing.

## Edit mode

With the Property Manager plugin, you can manage rules, behaviors, criteria, and variables in your existing configurations. To learn more about how these elements work together, see [PAPI Catalog ](https://developer.akamai.com/api/core_features/property_manager/vlatest.html).

### Add behaviors and criteria

1. From the **Property Manager** menu, select **Add Behavior/Criteria**.
2. Select one or more items from the list of options available for your product and module. This inserts an object template to the rule.

    ![addbehavior](/media/addbehavior.gif)

3. Specify the values with the help of inline suggestions. Suggestions appear in the line numbering or when you mouse over the rule tree elements.

![suggestions](/media/suggestions.gif)

> **NOTE**: For some configuration elements, you might need to access other Akamai resources outside of this extension, such as [CP codes](https://developer.akamai.com/api/core_features/cp_codes_reporting_groups/v1.html), [Site Shield maps](https://developer.akamai.com/api/cloud_security/site_shield/v1.html), or [hostnames](https://developer.akamai.com/api/core_features/edge_hostnames/v1.html). We plan to integrate external resources in future releases.

### Add a blank rule

You can further extend your configuration by selecting **Add Blank Rule** from the **Property Manager** menu. This option inserts an object template to the children array. Customize it by adding criteria to filter requests to your property and trigger selected behaviors. You can also nest rules in each other.


### Variables

The plugin supports both built-in and user-defined variables in your configuration. You can use them in the string values within `options` objects.

Insert an already defined variable to your rule tree by selecting **Use Variable** from the **Property Manager** menu.

To create a custom variable, select **Create Variable** from the **Property Manager** menu. A variable template appears in the variables section.


## Validation

Make sure your JSON file is correct before deploying it on the Akamai platform. The validation returns a list of errors and warnings that point you directly to the lines of code you need to fix.

To validate your configuration:

1. From the **Property Manager** menu, select **Validate Rule Tree**. This lists errors and warnings in the **Console** tab in the bottom pane.
2. Click the `errorLocation` value. The result points you to the line of code that caused the problem.


## Push the configuration back to the platform

After you’ve made your changes and validated the configuration, use [PAPI](https://learn.akamai.com/en-us/products/core_features/property_manager.html) or [Property Manager CLI](https://developer.akamai.com/cli/packages/property-manager.html) to push the updated file back to the Akamai platform.
