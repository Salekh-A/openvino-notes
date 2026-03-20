# Developer Guide

This documentation is intended for contributors working on `openvino-notes`.

The repository already has a meaningful CI and build setup, while the application code is still at an early implementation stage. The goal of these documents is to help contributors understand the project quickly and reproduce the same checks that gate pull requests and `main`.

## Recommended Reading Order

1. [Local CI Reproduction](./ci-local.md)
2. [Project Overview](./project.md)

## Current State

What is already in place:

- a four-module Android build
- reusable GitHub Actions workflows
- shared formatting, lint, and coverage policy

What is still mostly scaffolded:

- domain contracts
- data-layer behavior
- OpenVINO integration
- app-level product flows

## Main Work Areas

- Application code: `app`, `domain`, `data`, `ai`
- Automation and CI: `.github`
