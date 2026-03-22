 # @license GPL-3.0-or-later
 # Copyright (C) 2025 Caleb Gyamfi - Omnixys Technologies
 #
 # This program is free software: you can redistribute it and/or modify
 # it under the terms of the GNU General Public License as published by
 # the Free Software Foundation, either version 3 of the License, or
 # (at your option) any later version.
 #
 # This program is distributed in the hope that it will be useful,
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 # See the GNU General Public License for more details.
 #
 # For more information, visit <https://www.gnu.org/licenses/>.

# ---------------------------------------------------------------------------------------
# 🧱 docker-bake.hcl – Omnixys Bake Setup
# ---------------------------------------------------------------------------------------
# Build orchestration for Omnixys Node-based microservices using HashiCorp Docker Bake.
# Aufruf mit :
# APP_VERSION=$(grep -E '^version=' gradle.properties | cut -d'=' -f2 | tr -d '\r')
# REVISION=$(git rev-parse --short HEAD)
# docker buildx bake --set build.args.REVISION=$REVISION --set build.args.APP_VERSION=$APP_VERSION
# ---------------------------------------------------------------------------------------

variable "APP_NAME" {
  default = "logstream"
}

# Automatically use today's date (YYYY-MM-DD) as version tag
variable "APP_VERSION" {
  default = "dev"
}

variable "JAVA_VERSION" {
  default = "25"
}

variable "CREATED" {
  default = timestamp()
}

variable "REVISION" {
  default = "local-dev"
}

# ---------------------------------------------------------------------------------------
# Target Group
# ---------------------------------------------------------------------------------------

group "default" {
  targets = ["build"]
}

target "build" {
  dockerfile = "./Dockerfile"
  context = "."

   secret = [
      {
        id  = "gpr_user"
        src = ".secrets/gpr_user"
      },
      {
        id  = "gpr_key"
        src = ".secrets/gpr_key"
      }
    ]

  args = {
    JAVA_VERSION = "${JAVA_VERSION}"
    APP_NAME     = "${APP_NAME}"
    APP_VERSION  = "${APP_VERSION}"
    CREATED      = "${CREATED}"
    REVISION     = "${REVISION}"
  }

  labels = {
    "org.opencontainers.image.title"         = "omnixys-${APP_NAME}-service"
    "org.opencontainers.image.version"       = "${APP_VERSION}"
    "org.opencontainers.image.created"       = "${CREATED}"
    "org.opencontainers.image.revision"      = "${REVISION}"
    "org.opencontainers.image.source"        = "https://github.com/omnixys/omnixys-${APP_NAME}-service"
    "org.opencontainers.image.licenses"      = "GPL-3.0-or-later"
    "org.opencontainers.image.vendor"        = "omnixys"
    "org.opencontainers.image.authors"       = "caleb.gyamfi@omnixys.com"
  }

  tags = [
    "omnixys/${APP_NAME}-service:${APP_VERSION}"
  ]

platforms = ["linux/arm64"]
output = ["type=docker"]

}
