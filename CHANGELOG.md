# Changelog

All notable changes to this project will be documented in this file.

Format follows [Keep a Changelog](https://keepachangelog.com/) conventions.

→ [Türkçe CHANGELOG](CHANGELOG.tr.md)

## [Unreleased]

### Planned
- Python, JavaScript, TypeScript, Go, Rust language support
- Matching bracket highlight on cursor hover
- Indent guide colorization
- Scope highlighting
- HTML/XML tag pair colorization
- Unit tests

## [0.1.0] - 2026-05-14

### Added
- Bracket colorization for Java, Kotlin, XML, HTML, JSON
- 7-level color hierarchy
- Default colors for light (Default) and dark (Darcula) themes
- Customization via Settings → Editor → Color Scheme → Bracketbow
- Settings UI at Settings → Tools → Bracketbow (enable/disable, color levels, bracket types, languages)
- Modular architecture: `psi/`, `colors/`, `annotator/`, `settings/` packages
