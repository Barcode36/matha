USE [MathaNew]
GO

/****** Object:  Table [dbo].[States]    Script Date: 07-01-2018 16:52:06 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[State](
	[name] [varchar](50) NOT NULL
 CONSTRAINT [PK_States] PRIMARY KEY 
(
	[name] ASC
) ON [PRIMARY]
GO

INSERT INTO [dbo].[State] ([name])
     VALUES
           ('Kerala'),
           ('Tamil Nadu');
GO
