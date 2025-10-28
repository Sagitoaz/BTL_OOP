#!/usr/bin/env python3
"""
Script to add navigation buttons to all FXML files
Adds Back, Forward, and Reload buttons to the top of each scene
"""

import os
import re
from pathlib import Path

# Navigation bar component to insert
NAVIGATION_BAR = '''    <!-- Navigation Bar -->
    <top>
        <HBox alignment="CENTER_LEFT" spacing="10" styleClass="standalone-nav-bar">
            <padding>
                <Insets bottom="8.0" left="20.0" right="20.0" top="8.0" />
            </padding>
            <children>
                <Button styleClass="nav-button nav-back-button" text="◀ Back" fx:id="navBackButton" />
                <Button styleClass="nav-button nav-forward-button" text="Forward ▶" fx:id="navForwardButton" />
                <Button styleClass="nav-button nav-reload-button" text="⟳ Reload" fx:id="navReloadButton" />
                <Region HBox.hgrow="ALWAYS" />
            </children>
        </HBox>
    </top>
'''

# Files to skip (login, signup, etc.)
SKIP_FILES = ['Login.fxml', 'Signup.fxml', 'ResetPassword.fxml', 'ChangePassword.fxml']

def add_navigation_css(fxml_content):
    """Add navigation.css to stylesheets if not already present"""
    if 'navigation.css' in fxml_content:
        return fxml_content
    
    # Find stylesheets attribute
    pattern = r'(stylesheets="[^"]*)"'
    match = re.search(pattern, fxml_content)
    
    if match:
        old_stylesheets = match.group(1)
        new_stylesheets = old_stylesheets[:-1] + ',@../../css/navigation.css"'
        fxml_content = fxml_content.replace(old_stylesheets, new_stylesheets)
    
    return fxml_content

def has_navigation_bar(fxml_content):
    """Check if navigation bar already exists"""
    return 'nav-back-button' in fxml_content or 'navBackButton' in fxml_content

def add_imports(fxml_content):
    """Add necessary imports for navigation components"""
    imports_to_add = []
    
    if 'import javafx.geometry.Insets' not in fxml_content:
        imports_to_add.append('<?import javafx.geometry.Insets?>')
    if 'import javafx.scene.layout.Region' not in fxml_content:
        imports_to_add.append('<?import javafx.scene.layout.Region?>')
    
    if imports_to_add:
        # Find the last import line
        last_import_match = list(re.finditer(r'\?import [^?]+\?>', fxml_content))
        if last_import_match:
            insert_pos = last_import_match[-1].end()
            fxml_content = fxml_content[:insert_pos] + '\n' + '\n'.join(imports_to_add) + fxml_content[insert_pos:]
    
    return fxml_content

def add_navigation_to_borderpane(fxml_content):
    """Add navigation bar to BorderPane layout"""
    if has_navigation_bar(fxml_content):
        print("  ✓ Navigation bar already exists, skipping...")
        return fxml_content
    
    # Find BorderPane opening tag
    borderpane_pattern = r'(<BorderPane[^>]*>)\s*\n'
    match = re.search(borderpane_pattern, fxml_content)
    
    if match:
        insert_pos = match.end()
        fxml_content = fxml_content[:insert_pos] + '\n' + NAVIGATION_BAR + '\n' + fxml_content[insert_pos:]
        print("  ✓ Added navigation bar to BorderPane")
    else:
        print("  ✗ BorderPane not found")
    
    return fxml_content

def add_navigation_to_vbox(fxml_content):
    """Add navigation bar to VBox layout (top-level)"""
    if has_navigation_bar(fxml_content):
        print("  ✓ Navigation bar already exists, skipping...")
        return fxml_content
    
    # Find top-level VBox opening tag
    vbox_pattern = r'(<VBox[^>]*xmlns[^>]*>)\s*\n\s*(<children>)?\s*\n'
    match = re.search(vbox_pattern, fxml_content)
    
    if match:
        # Simplified navigation for VBox (no <top> tag needed)
        nav_bar_simple = '''        <HBox alignment="CENTER_LEFT" spacing="10" styleClass="standalone-nav-bar">
            <padding>
                <Insets bottom="8.0" left="20.0" right="20.0" top="8.0" />
            </padding>
            <children>
                <Button styleClass="nav-button nav-back-button" text="◀ Back" fx:id="navBackButton" />
                <Button styleClass="nav-button nav-forward-button" text="Forward ▶" fx:id="navForwardButton" />
                <Button styleClass="nav-button nav-reload-button" text="⟳ Reload" fx:id="navReloadButton" />
                <Region HBox.hgrow="ALWAYS" />
            </children>
        </HBox>
'''
        if '<children>' in match.group(0):
            insert_pos = match.end()
        else:
            # Add <children> tag if it doesn't exist
            insert_pos = match.end()
            nav_bar_simple = '    <children>\n' + nav_bar_simple
        
        fxml_content = fxml_content[:insert_pos] + '\n' + nav_bar_simple + '\n' + fxml_content[insert_pos:]
        print("  ✓ Added navigation bar to VBox")
    else:
        print("  ✗ VBox not found")
    
    return fxml_content

def process_fxml_file(file_path):
    """Process a single FXML file"""
    filename = os.path.basename(file_path)
    
    # Skip certain files
    if filename in SKIP_FILES:
        print(f"⊘ Skipping {filename}")
        return
    
    print(f"\n📄 Processing {filename}...")
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Add navigation.css to stylesheets
        content = add_navigation_css(content)
        
        # Add necessary imports
        content = add_imports(content)
        
        # Try to add navigation bar based on layout type
        if '<BorderPane' in content:
            content = add_navigation_to_borderpane(content)
        elif '<VBox' in content and 'xmlns' in content[:500]:  # Top-level VBox
            content = add_navigation_to_vbox(content)
        else:
            print("  ⚠ Unsupported layout type, skipping...")
        
        # Only write if content changed
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"  ✅ Updated {filename}")
        else:
            print(f"  → No changes needed for {filename}")
    
    except Exception as e:
        print(f"  ❌ Error processing {filename}: {str(e)}")

def main():
    """Main function to process all FXML files"""
    base_dir = Path(__file__).parent.parent / 'resources' / 'FXML'
    
    if not base_dir.exists():
        print(f"❌ Directory not found: {base_dir}")
        return
    
    print("🚀 Starting navigation bar injection...")
    print(f"📁 Base directory: {base_dir}")
    
    # Find all FXML files recursively
    fxml_files = list(base_dir.rglob('*.fxml'))
    
    print(f"\n📊 Found {len(fxml_files)} FXML files\n")
    
    for fxml_file in fxml_files:
        process_fxml_file(fxml_file)
    
    print("\n\n✅ Navigation bar injection complete!")
    print(f"📝 Processed {len(fxml_files)} files")

if __name__ == '__main__':
    main()
