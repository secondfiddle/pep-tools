# ![PEP Tools logo](docs/images/pep-tools-icon.png?raw=true) PEP Tools: PDE/Eclipse Productivity

The plugins in PEP Tools are intended to help Eclipse RCP developers get more out of their IDE. It raises the level of support for features and products to be equivalent to the built-in support for plugins, and includes time-saving features for plugin development.

To install PEP Tools either use the [Eclipse Marketplace](https://marketplace.eclipse.org/content/pep-tools-pdeeclipse-productivity):

  1. Open the Marketplace client from the `Help > Eclipse Marketplace...` menu option.
  2. Search for "PEP".
  3. Click the `Install` button for the PEP Tools search-result.
  4. Follow the wizard through to completion.

Or install directly from the update site:

  1. Open the `Install` dialog from the `Help > Install New Software...` menu option.
  2. Paste http://www.secondfiddle.org.uk/pep-tools/update-site/ into the `Work with` text-box and press enter.
  3. Check the box next the "PEP Tools" feature.
  4. Click `Next` and follow the wizard through to completion.


## Feature Explorer

The Feature Explorer view is intended to provide equivalent functionality to the Plug-in Dependencies view included with PDE, but for features and products. It also supports various types of refactoring, taking into account more references to the artifact being refactored than Eclipse's built-in refactoring.

The view can be opened from the `Window > Show View > Other...` dialog, in the `Plug-in Development` category:

  ![Opening Feature Explorer](docs/images/feature-explorer-opening.png?raw=true "Opening Feature Explorer")

By default the view only shows features, and any features contained within other features are listed under their parents rather than at the top level. Any part of a feature-name may be entered in the search-box to filter the view's contents.

  ![Feature Explorer view](docs/images/feature-explorer-view.png?raw=true "Feature Explorer view")

The buttons on the view's toolbar allow:
  * The features including each feature to be shown, rather than the features included in each feature
  * Features included in other features/products to be shown at the top-level, rather than just under their parents
  * Plugins and fragments included within a feature to be shown
  * Products that are based on features to be shown (in a similar manner to parent features)

Double-clicking a feature, product or plugin in the view opens an editor for that artifact.  

#### Refactoring

  * Drag-and-drop may be used to copy plugin/feature inclusions to other features/products
  * Feature or plugin projects may be dragged from other views (e.g. Package Explorer) and dropped onto features/products to include them in those artifacts
  * Copying and pasting can also be used in place of drag-and-drop
  * Renaming of a feature or plugin (via the context-menu) will ensure that all references are updated and also rename/move the relevant feature/plugin project
  * Features and products can also be deleted via the context-menu


## Product Validation

When developing Eclipse RCP applications developers can come across issues that prevent the successful launching of a product. Often the cause of these issues is not immediately apparent but can be tracked down using the `Validate Plug-ins` button hidden away in the product's generated launch configuration.

PEP Tools' product support includes a nature and builder for products to ensure that these problems are more easily found. In order to add the product nature/builder to a project simply right-click a product file in the Package Explorer view and choose `Plug-in Tools > Add Product Nature`. This can also be done from the Feature Explorer view, through the equivalent `Add Product Nature` context-menu option.

  ![Adding the product nature](docs/images/product-nature-adding.png?raw=true "Adding the product nature")

Any problems with the product will then be listed in the Problems view in the same way as plugin, feature or Java problems:

  ![Product nature validation](docs/images/product-nature-validation.png?raw=true "Product nature validation")


## Product Editor

PDE's plugin and feature editors helpfully include a tab that allows manual editing of the files being manipulated by the editors' main tabs. Unfortunately the product editor doesn't, but PEP Tools' product editor remedies this:

  ![Product editor](docs/images/product-editor-view.png?raw=true "Product editor")

The view sets itself as the default viewer for product-files but Eclipse will remember if another editor has previously been used, so it may be necessary to open the view manually to begin with:

  ![Opening the product editor](docs/images/product-editor-opening.png?raw=true "Opening the product editor")


## Plugin Dependency Calculation

Developers working on large RCP applications with many plugins (i.e. 100s) may have found that they spend a lot of time waiting for an Eclipse job labelled "Updating plug-in dependencies" to complete. This is an acknowledged performance issue with Eclipse 3.7+ that is [still present in Eclipse 4.4](https://bugs.eclipse.org/bugs/show_bug.cgi?id=355939 "Eclipse bug-tracker").

An experimental workaround for this issue is included in PEP Tools, and can markedly shorten both the run-time of the dependencies job and the subsequent Java build (which is due to the job no longer identifying that more plugins are affected by a dependency change than actually are).


## Project Templates

When working on a project that involves regular creation of Eclipse projects there are often conventions that are followed to ensure that these projects are consistent and build properly. Luckily Eclipse has various extension points to help with this:

  * [org.eclipse.pde.ui.templates](http://help.eclipse.org/kepler/topic/org.eclipse.pde.doc.user/reference/extension-points/org_eclipse_pde_ui_templates.html "Eclipse documentation")
  * [org.eclipse.pde.ui.pluginContent](http://help.eclipse.org/kepler/topic/org.eclipse.pde.doc.user/reference/extension-points/org_eclipse_pde_ui_pluginContent.html "Eclipse documentation")
  * [org.eclipse.pde.ui.newExtension](http://help.eclipse.org/kepler/topic/org.eclipse.pde.doc.user/reference/extension-points/org_eclipse_pde_ui_newExtension.html "Eclipse documentation")
  * [org.eclipse.ui.newWizards](http://help.eclipse.org/kepler/topic/org.eclipse.platform.doc.isv/guide/dialogs_wizards_newWizards.htm "Eclipse documentation")

While these can help, the first 3 options can't easily affect *all* elements of a new project, and the final one involves writing code and building/installing a plugin into the IDE.

PEP Tools' project templates build on the support already in Eclipse to allow declarative contribution of templates from within the Eclipse workspace. This makes adding or changing templates much simpler, and means that templates can be included in your codebase and kept up to date alongside the artifacts derived from them.

#### Creating a templates project

  * Create a new general project: `File > New > Project...`, then `General > Project`
  * Update the `.project` file for the project to include the PEP Tools project-template nature - the file should look similar to the below:

```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <projectDescription>
        <name>templates</name>
        <comment></comment>
        <projects>
        </projects>
        <buildSpec>
        </buildSpec>
        <natures>
            <nature>uk.org.secondfiddle.pep.projects.templates.nature.ProjectTemplateNature</nature>
        </natures>
    </projectDescription>
```

  * One or more templates may now be added to this project, in their own subdirectories
  * Each template subdirectory should have the following structure:

|Filename           |Type           |Required?  |Description  |
|-------------------|---------------|-----------|-------------|
|template.mf        |Manifest file  |Yes        |Specifies the template's name, parameters, and other properties|
|template/          |Directory      |No         |Contains any artifacts to be created by the template|
|template/bin/      |Directory      |No         |Contains any binary artifacts to be created by the template - these artifacts will be placed in the root of the new project but will not go through the parameter substitution that other resources in the template go through|
|template/java/     |Directory      |No         |Contains any Java artifacts to be created by the template - this directory will be substituted for the main package directory of the project (see `pluginId` and `packageName` below)|
|template/**/empty  |Empty file     |No         |A marker file to specify that its otherwise-empty parent directory should be created by the template|

  ![Project templates example](docs/images/project-templates-example.png?raw=true "Project templates example")

#### Creating a template manifest

Template manifests specify the name, parameters, and other properties of the template. An example manifest follows:

```
Manifest-Version: 1.0
TemplateName: Example Plugin
TemplateGroup: Example Group
TemplateIconSmall: icon_16x16.png
TemplateIconLarge: icon_64x64.png

Name: mainClass
Label: Main class-name
Type: String

Name: repository
Label: Repository
Type: Directory
Preference: com.example.plugin:com.example.plugin.preference

Name: projectLocation
Type: Hidden
DefaultValue: $repository$/plugins/$pluginId$
```

The manifest is divided into a main attributes block followed by several named attributes blocks, one for each template parameter. The main attributes are as follows:

|Attribute          |Required?  |Description  |
|-------------------|-----------|-------------|
|TemplateName       |Yes        |The template's name, as presented in the UI|
|TemplateGroup      |No         |The template's group or category, as presented in the UI (if unspecified the template will be listed in the "Other" category)|
|TemplateIconSmall  |No         |A relative path to a 16x16 PNG file to use as an icon for the template on the first page of the "New Project" dialog|
|TemplateIconLarge  |No         |A relative path to a 64x64 PNG file to use as a header-image for the template on the second page of the "New Project" dialog|
|TemplateExtends    |No         |An existing template to extend with additional parameters and resources, specified in the form `{template-project-name}:{template-name}`, e.g. `base-templates:Base Project`|

Each template parameter block may have the following attributes:

|Attribute       |Required?  |Description  |
|----------------|-----------|-------------|
|Name            |Yes        |Lower-camel-cased name to use to reference the parameter within template files, e.g. `myParam`, which would be referred to in template files as `$myParam$`|
|Label           |Yes        |The label to use for the parameter's form-field|
|Type            |Yes        |The type of form-field to display for the parameter (see supported types below)|
|Preference      |No         |An instance/workspace-scope preference to load a default value for the parameter from and save any new value to, in the form `{plugin-name}:{preference-name}`, e.g. `com.example.plugin:com.example.plugin.preference`|
|DefaultValue    |No         |A default value for the parameter|
|ValueFilter     |No         |A regular expression that combo/select-box options must match in order to be displayed, in the form `/{regex}/`, e.g. `/^some-prefix.*/`|
|ValueMapping    |No         |A regular expression and replacement string to translate from a value entered in a form-field to the final parameter value, in the form `/{regex}/{replacement}/`|
|DisplayMapping  |No         |A regular expression and replacement string to translate from a parameter value option to the option string to be displayed, in the form `/{regex}/{replacement}/`|
|Options         |No         |A comma-separated list of values to use as combo/select-box options|

When using the "New Project" wizard with a template, a form-field is displayed for "Project name" followed by one for each parameter defined in the manifest.

  ![Project templates parameters](docs/images/project-templates-parameters.png?raw=true "Project templates parameters")

The supported field-types are as follows:

|Type        |Description  |
|------------|-------------|
|String      |Simple text-field|
|Directory   |Text-field with "Browse" button to select a directory|
|Select      |Select-box displaying options specified through the `Options` attribute (see above)|
|Combo       |Combo-box (i.e. select-box allowing text entry) displaying options specified through the `Options` attribute (see above)|
|WorkingSet  |Combo-box displaying all existing working-sets and allowing entry of a name for a new working-set|
|Hidden      |Hidden field, useful for building new parameter values from multiple existing ones using the `DefaultValue` attribute (see above)|

#### Creating a template file

All files in the `template/` and `template/java/` directories (and sub-directories) go through parameter value substitution when a new templated project is created. This replaces every string of the form `${parameterName}$` (e.g. `$exampleParam$`) with the named parameter's value, in both file-names and file contents.

In addition to custom parameters specified in the template manifest, some "special" built-in ones may be used:

|Parameter        |Specifiable in manifest?  |Description  |
|-----------------|--------------------------|-------------|
|pluginId         |No                        |The project's name, as specified in the first form-field in the UI|
|packageName      |Yes                       |The base package name for the Java files in the project (defaulted to the value of `pluginId`)|
|projectLocation  |Yes                       |The location in which to create the project (defaulted to being within the Eclipse workspace, just as for standard Eclipse projects)|
|workingSet       |Yes                       |A working-set to which the newly created project is added|

When referring to parameters in template files, simple transformations of parameter values are possible using the syntax `${parameterName}:{transformation}$`, e.g. `$exampleParam:uppercamelcase$`.

|Transformation  |Description  |
|----------------|-------------|
|unmapped        |For parameters with a `ValueMapping` attribute, the value prior to the value-mapping being applied|
|uppercamelcase  |Transforms a value to be upper camel-cased (e.g. `Example Value` becomes `ExampleValue`)|
|lowercamelcase  |Transforms a value to be lower camel-cased (e.g. `Example Value` becomes `exampleValue`)|

#### Using a template

Once created, a template may be used through Eclipse's standard "New Project" wizard:  `File > New > Project...`.

  ![Using a project template](docs/images/project-templates-using.png?raw=true "Using a project template")

